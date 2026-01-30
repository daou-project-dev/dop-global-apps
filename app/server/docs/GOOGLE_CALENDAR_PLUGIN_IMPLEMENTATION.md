# Google Calendar 플러그인 구현

## 개요

- **목표**: PF4J 플러그인 형태로 Google Calendar API 연동
- **인증 방식**: Service Account + JSON 키 파일 (Domain-Wide Delegation)
- **패턴**: Handler 패턴 적용

---

## 의존성

### libs.versions.toml

```toml
[versions]
google-api-client = "2.7.0"
google-oauth-client = "1.36.0"
google-api-services-calendar = "v3-rev20241101-2.0.0"
google-auth-library = "1.30.0"

[libraries]
google-api-client = { module = "com.google.api-client:google-api-client", version.ref = "google-api-client" }
google-oauth-client-jetty = { module = "com.google.oauth-client:google-oauth-client-jetty", version.ref = "google-oauth-client" }
google-api-services-calendar = { module = "com.google.apis:google-api-services-calendar", version.ref = "google-api-services-calendar" }
google-auth-library-oauth2-http = { module = "com.google.auth:google-auth-library-oauth2-http", version.ref = "google-auth-library" }
```

### 플러그인 build.gradle

```groovy
plugins {
    id 'java'
}

dependencies {
    compileOnly project(':plugins:plugin-sdk')
    compileOnly(libs.pf4j)
    annotationProcessor(libs.pf4j)

    implementation(libs.google.api.client)
    implementation(libs.google.api.services.calendar)
    implementation(libs.google.auth.library.oauth2.http)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly 'org.slf4j:slf4j-api:2.0.16'
}

jar {
    manifest {
        attributes 'Plugin-Class': 'com.daou.dop.gapps.plugin.google.calendar.GoogleCalendarPlugin',
                   'Plugin-Id': 'google-calendar-plugin',
                   'Plugin-Version': '0.0.1',
                   'Plugin-Provider': 'Daou Tech'
    }
}
```

---

## 모듈 구조

```
plugins/google-calendar-plugin/src/main/java/com/daou/dop/global/apps/plugin/google/calendar/
├── GoogleCalendarPlugin.java           # PF4J 진입점
├── GoogleCalendarPluginExecutor.java   # PluginExecutor 구현 (Handler 위임)
├── handler/
│   ├── ActionHandler.java              # Handler 인터페이스
│   ├── BaseHandler.java                # 공통 유틸 (변환 메서드)
│   ├── CalendarListHandler.java        # calendar.list
│   ├── EventsListHandler.java          # events.list
│   ├── EventsGetHandler.java           # events.get
│   ├── EventsCreateHandler.java        # events.create
│   ├── EventsUpdateHandler.java        # events.update
│   └── EventsDeleteHandler.java        # events.delete
└── service/
    ├── GoogleAuthService.java          # 인증 처리 (JSON 키 → Credentials)
    └── GoogleCalendarService.java      # Calendar API 호출
```

---

## 지원 Action

| Action | 설명 | 필수 파라미터 |
|--------|------|---------------|
| `calendar.list` | 캘린더 목록 조회 | - |
| `events.list` | 일정 목록 조회 | calendarId |
| `events.get` | 일정 단건 조회 | calendarId, eventId |
| `events.create` | 일정 생성 | calendarId, event |
| `events.update` | 일정 수정 | calendarId, eventId, event |
| `events.delete` | 일정 삭제 | calendarId, eventId |

---

## 핵심 클래스

### 1. ActionHandler 인터페이스

```java
public interface ActionHandler {
    String getAction();
    ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService);
}
```

### 2. GoogleCalendarPluginExecutor

```java
@Extension
public class GoogleCalendarPluginExecutor implements PluginExecutor {
    private final Map<String, ActionHandler> handlers = new HashMap<>();

    public GoogleCalendarPluginExecutor() {
        registerHandler(new CalendarListHandler());
        registerHandler(new EventsListHandler());
        // ...
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        ActionHandler handler = handlers.get(request.action());
        return handler.handle(request, calendarService);
    }
}
```

### 3. GoogleAuthService

```java
public class GoogleAuthService {
    // JSON 키 파일에서 Credentials 생성
    public Calendar createCalendarService(CredentialContext credential) {
        GoogleCredentials credentials = loadCredentialsFromFile(jsonKeyPath);
        credentials = credentials.createScoped(CalendarScopes.CALENDAR);
        credentials.refreshIfExpired();
        return buildCalendarService(credentials);
    }
}
```

### 4. GoogleCalendarService

```java
public class GoogleCalendarService {
    public CalendarList listCalendars();
    public Events listEvents(calendarId, timeMin, timeMax, ...);
    public Event getEvent(calendarId, eventId);
    public Event createEvent(calendarId, eventData);
    public Event updateEvent(calendarId, eventId, eventData);
    public void deleteEvent(calendarId, eventId);
}
```

---

## 인증 방식

### Service Account + Domain-Wide Delegation

```
┌─────────────────────────────────────────────────────────────────┐
│  Google Workspace 관리자 설정                                    │
├─────────────────────────────────────────────────────────────────┤
│  1. GCP 프로젝트 생성                                            │
│  2. Service Account 생성                                         │
│  3. JSON 키 파일 다운로드                                         │
│  4. Google Workspace 관리 콘솔에서 Domain-Wide Delegation 설정   │
│     - Client ID 등록                                             │
│     - Scope 허용 (calendar, calendar.readonly)                   │
└─────────────────────────────────────────────────────────────────┘
```

### CredentialContext 매핑

| 필드 | 용도 |
|------|------|
| `apiKey` | JSON 키 파일 경로 또는 내용 |
| `externalId` | 위임 대상 사용자 이메일 |
| `metadata.keyType` | "file_path", "base64", "adc" |

---

## 에러 처리

| Google API 에러 | HTTP 코드 | ExecuteResponse |
|----------------|-----------|-----------------|
| `UNAUTHENTICATED` | 401 | `error(401, "Authentication failed")` |
| `PERMISSION_DENIED` | 403 | `error(403, "Permission denied")` |
| `NOT_FOUND` | 404 | `error(404, "Resource not found")` |
| `INVALID_ARGUMENT` | 400 | `error(400, "Invalid request")` |
| `RESOURCE_EXHAUSTED` | 429 | `error(429, "Rate limit exceeded")` |

---

## DB 설정

### plugin 테이블

```sql
INSERT INTO plugin (plugin_id, name, description, auth_type, status)
VALUES (
    'google-calendar',
    'Google Calendar',
    'Google Workspace Calendar 연동',
    'SERVICE_ACCOUNT',
    'ACTIVE'
);
```

### AuthType Enum

```java
public enum AuthType {
    OAUTH2,           // OAuth 2.0 인증
    API_KEY,          // API Key 인증
    SERVICE_ACCOUNT   // Service Account (JSON 키 파일) 인증
}
```

---

## Slack vs Google Calendar 비교

| 항목 | Slack | Google Calendar |
|------|-------|-----------------|
| **인증 방식** | OAuth 2.0 | Service Account |
| **토큰 관리** | access_token + refresh_token | JSON 키 파일 |
| **연동 단위** | Workspace (teamId) | Workspace + User (delegation) |
| **토큰 갱신** | refresh_token 사용 | credentials.refreshIfExpired() |
| **DB 테이블** | oauth_credential | apikey_credential |

---

## 관련 문서

- [GOOGLE_CALENDAR_PLUGIN_PLAN.md](./GOOGLE_CALENDAR_PLUGIN_PLAN.md) - 구현 계획
- [GOOGLE_CALENDAR_INTEGRATION_TEST.md](./GOOGLE_CALENDAR_INTEGRATION_TEST.md) - 연동 테스트 가이드
