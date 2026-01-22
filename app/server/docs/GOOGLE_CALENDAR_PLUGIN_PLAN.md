# Google Workspace Calendar 플러그인 구현 계획

## 개요

- **목표**: PF4J 플러그인 형태로 Google Workspace Calendar 연동 구현
- **인증 방식**: Service Account + JSON 키 파일 (OAuth 아님)
- **연동 단위**: 고객사별 JSON 키 파일 관리, 사용자별 위임(delegation)

---

## 1. 의존성 추가

### libs.versions.toml

```toml
[versions]
google-api-client = "2.7.0"
google-oauth-client = "1.36.0"
google-api-services-calendar = "v3-rev20241101-2.0.0"

[libraries]
google-api-client = { module = "com.google.api-client:google-api-client", version.ref = "google-api-client" }
google-oauth-client-jetty = { module = "com.google.oauth-client:google-oauth-client-jetty", version.ref = "google-oauth-client" }
google-api-services-calendar = { module = "com.google.apis:google-api-services-calendar", version.ref = "google-api-services-calendar" }
google-auth-library-oauth2-http = { module = "com.google.auth:google-auth-library-oauth2-http", version = "1.30.0" }
```

### plugins/google-calendar-plugin/build.gradle

```groovy
plugins {
    id 'java'
}

dependencies {
    compileOnly project(':plugins:plugin-sdk')
    compileOnly(libs.pf4j)
    annotationProcessor(libs.pf4j)

    // Google Calendar API
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.calendar)
    implementation(libs.google.auth.library.oauth2.http)

    // Lombok
    compileOnly 'org.projectlombok:lombok:1.18.42'
    annotationProcessor 'org.projectlombok:lombok:1.18.42'

    // SLF4J
    compileOnly 'org.slf4j:slf4j-api:2.0.16'
}

jar {
    manifest {
        attributes 'Plugin-Class': 'com.daou.dop.global.apps.plugin.google.calendar.GoogleCalendarPlugin',
                   'Plugin-Id': 'google-calendar-plugin',
                   'Plugin-Version': '0.0.1',
                   'Plugin-Provider': 'Daou Tech'
    }
}
```

---

## 2. 인증 방식

### 2.1 Service Account + Domain-Wide Delegation

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

### 2.2 인증 흐름

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ JSON 키 파일  │ ──▶ │ GoogleCredentials │ ──▶ │ Calendar Service │
│ (byte[])     │     │ + Delegation     │     │                  │
└──────────────┘     └──────────────────┘     └──────────────────┘
       │                     │
       │                     ▼
       │              createScoped()
       │              createDelegated(userEmail)
       │              refreshIfExpired()
       │
       ▼
  파일 경로로 로드
  또는 DB에서 조회
```

### 2.3 인증 코드 예시

```java
// JSON 키 파일에서 Credentials 생성
GoogleCredentials credentials = GoogleCredentials
    .fromStream(new ByteArrayInputStream(jsonKeyBytes))
    .createScoped(Collections.singletonList(CalendarScopes.CALENDAR))
    .createDelegated(delegatedUserEmail);

// 토큰 갱신
credentials.refreshIfExpired();

// Calendar 서비스 생성
Calendar calendarService = new Calendar.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory.getDefaultInstance(),
        new HttpCredentialsAdapter(credentials))
    .setApplicationName("DaouOffice-GlobalApps")
    .build();
```

---

## 3. 플러그인 구조

### 3.1 패키지 구조

```
plugins/google-calendar-plugin/
├── build.gradle
└── src/main/java/com/daou/dop/global/apps/plugin/google/calendar/
    ├── GoogleCalendarPlugin.java           # PF4J Plugin 진입점
    ├── GoogleCalendarPluginExecutor.java   # PluginExecutor 구현
    ├── service/
    │   ├── GoogleCalendarService.java      # Calendar API 호출
    │   └── GoogleAuthService.java          # 인증 처리
    └── dto/
        ├── CalendarEventRequest.java       # 일정 생성/수정 요청
        └── CalendarEventResponse.java      # 일정 응답
```

### 3.2 클래스 다이어그램

```
┌─────────────────────────────────┐
│ GoogleCalendarPlugin            │
│ extends Plugin                  │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│ GoogleCalendarPluginExecutor    │
│ implements PluginExecutor       │
├─────────────────────────────────┤
│ + getPluginId(): String         │
│ + getSupportedActions(): List   │
│ + execute(request): Response    │
└─────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────┐
│ GoogleCalendarService           │
├─────────────────────────────────┤
│ + listCalendars()               │
│ + listEvents()                  │
│ + getEvent()                    │
│ + createEvent()                 │
│ + updateEvent()                 │
│ + deleteEvent()                 │
└─────────────────────────────────┘
```

---

## 4. API 엔드포인트

### 4.1 Execute API 호출 방식

모든 Google Calendar API는 `/execute` 엔드포인트를 통해 호출

```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "calendar.list",
    "connectionId": 123,
    "params": { ... }
  }'
```

### 4.2 지원 Action 목록

| Action | 설명 | 필수 파라미터 |
|--------|------|---------------|
| `calendar.list` | 캘린더 목록 조회 | - |
| `events.list` | 일정 목록 조회 | calendarId |
| `events.get` | 일정 단건 조회 | calendarId, eventId |
| `events.create` | 일정 생성 | calendarId, event |
| `events.update` | 일정 수정 | calendarId, eventId, event |
| `events.delete` | 일정 삭제 | calendarId, eventId |

---

## 5. API 상세

### 5.1 캘린더 목록 조회 (calendar.list)

**요청**:
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "calendar.list",
    "connectionId": 123,
    "params": {}
  }'
```

**응답**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": {
    "calendars": [
      {
        "id": "primary",
        "summary": "user@company.com",
        "primary": true
      },
      {
        "id": "team@company.com",
        "summary": "팀 캘린더",
        "primary": false
      }
    ]
  }
}
```

### 5.2 일정 목록 조회 (events.list)

**요청**:
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "events.list",
    "connectionId": 123,
    "params": {
      "calendarId": "primary",
      "timeMin": "2026-01-01T00:00:00+09:00",
      "timeMax": "2026-01-31T23:59:59+09:00",
      "maxResults": 50,
      "orderBy": "startTime",
      "singleEvents": true
    }
  }'
```

**응답**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": {
    "events": [
      {
        "id": "event123",
        "summary": "팀 미팅",
        "description": "주간 회의",
        "start": {
          "dateTime": "2026-01-20T10:00:00+09:00",
          "timeZone": "Asia/Seoul"
        },
        "end": {
          "dateTime": "2026-01-20T11:00:00+09:00",
          "timeZone": "Asia/Seoul"
        },
        "location": "회의실 A",
        "attendees": [
          {"email": "user1@company.com", "responseStatus": "accepted"},
          {"email": "user2@company.com", "responseStatus": "needsAction"}
        ]
      }
    ],
    "nextPageToken": "token123"
  }
}
```

### 5.3 일정 단건 조회 (events.get)

**요청**:
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "events.get",
    "connectionId": 123,
    "params": {
      "calendarId": "primary",
      "eventId": "event123"
    }
  }'
```

### 5.4 일정 생성 (events.create)

**요청**:
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "events.create",
    "connectionId": 123,
    "params": {
      "calendarId": "primary",
      "event": {
        "summary": "신규 미팅",
        "description": "프로젝트 킥오프",
        "start": {
          "dateTime": "2026-01-25T14:00:00+09:00",
          "timeZone": "Asia/Seoul"
        },
        "end": {
          "dateTime": "2026-01-25T15:00:00+09:00",
          "timeZone": "Asia/Seoul"
        },
        "location": "회의실 B",
        "attendees": [
          {"email": "user1@company.com"},
          {"email": "user2@company.com"}
        ]
      }
    }
  }'
```

**응답**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": {
    "id": "newEvent456",
    "summary": "신규 미팅",
    "htmlLink": "https://www.google.com/calendar/event?eid=xxx"
  }
}
```

### 5.5 일정 수정 (events.update)

**요청**:
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "events.update",
    "connectionId": 123,
    "params": {
      "calendarId": "primary",
      "eventId": "event123",
      "event": {
        "summary": "미팅 (시간 변경)",
        "start": {
          "dateTime": "2026-01-25T15:00:00+09:00",
          "timeZone": "Asia/Seoul"
        },
        "end": {
          "dateTime": "2026-01-25T16:00:00+09:00",
          "timeZone": "Asia/Seoul"
        }
      }
    }
  }'
```

### 5.6 일정 삭제 (events.delete)

**요청**:
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "events.delete",
    "connectionId": 123,
    "params": {
      "calendarId": "primary",
      "eventId": "event123"
    }
  }'
```

**응답**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": {
    "deleted": true,
    "eventId": "event123"
  }
}
```

---

## 6. 인증 정보 관리

### 6.1 JSON 키 파일 저장 방식

| 방식 | 장점 | 단점 |
|------|------|------|
| **파일 시스템** | 간단, 빠름 | 서버 간 공유 어려움 |
| **DB (BLOB)** | 중앙 관리, 암호화 가능 | 용량 증가 |
| **외부 저장소 (S3)** | 확장성 | 복잡도 증가 |

### 6.2 apikey_credential 테이블 활용

```sql
-- Google Calendar은 API Key 방식으로 분류
INSERT INTO apikey_credential (
    connection_id,
    api_key,           -- JSON 키 파일 경로 또는 내용 (암호화)
    api_secret,        -- NULL (사용 안 함)
    created_at
) VALUES (
    123,
    '/path/to/service-account.json',  -- 또는 Base64 인코딩된 JSON
    NULL,
    NOW()
);
```

### 6.3 CredentialContext 구조

```java
// 서버에서 플러그인으로 전달되는 인증 정보
CredentialContext {
    accessToken: null,           // OAuth 아님
    refreshToken: null,
    apiKey: "/path/to/key.json", // JSON 키 파일 경로
    externalId: "user@company.com",  // delegatedUserEmail
    metadata: {
        "keyType": "file_path",  // 또는 "base64"
        "projectId": "my-project"
    }
}
```

---

## 7. 데이터 모델

### 7.1 plugin 테이블 데이터

```sql
INSERT INTO plugin (
    plugin_id,
    name,
    description,
    auth_type,
    client_id,
    client_secret,
    secrets,
    metadata,
    status
) VALUES (
    'google-calendar',
    'Google Calendar',
    'Google Workspace Calendar 연동',
    'API_KEY',        -- Service Account 방식
    NULL,
    NULL,
    '{}',
    '{
      "scopes": ["https://www.googleapis.com/auth/calendar"],
      "api_base_url": "https://www.googleapis.com/calendar/v3"
    }',
    'ACTIVE'
);
```

### 7.2 plugin_connection 데이터

```sql
INSERT INTO plugin_connection (
    plugin_id,
    company_id,
    user_id,
    scope_type,
    external_id,
    external_name,
    metadata,
    status
) VALUES (
    'google-calendar',
    1,                          -- company_id
    NULL,                       -- WORKSPACE 레벨
    'WORKSPACE',
    'company-domain.com',       -- Google Workspace 도메인
    'A사 Google Workspace',
    '{
      "projectId": "my-gcp-project",
      "serviceAccountEmail": "sa@my-project.iam.gserviceaccount.com"
    }',
    'ACTIVE'
);
```

---

## 8. 에러 처리

### 8.1 에러 코드 매핑

| Google API 에러 | HTTP 코드 | 원인 | ExecuteResponse |
|----------------|-----------|------|-----------------|
| `UNAUTHENTICATED` | 401 | 토큰 만료/잘못됨 | `error(401, "Authentication failed")` |
| `PERMISSION_DENIED` | 403 | Domain-Wide Delegation 미설정 | `error(403, "Permission denied")` |
| `NOT_FOUND` | 404 | 캘린더/일정 없음 | `error(404, "Resource not found")` |
| `INVALID_ARGUMENT` | 400 | 잘못된 파라미터 | `error(400, "Invalid request")` |
| `RESOURCE_EXHAUSTED` | 429 | Rate Limit | `error(429, "Rate limit exceeded")` |

### 8.2 에러 응답 예시

```json
{
  "success": false,
  "statusCode": 403,
  "body": null,
  "error": "Permission denied: Domain-Wide Delegation not configured for this user"
}
```

---

## 9. 구현 순서

### Phase 1: 기본 구조

- [ ] `plugins/google-calendar-plugin` 디렉토리 생성
- [ ] `build.gradle` 설정 (의존성 추가)
- [ ] `settings.gradle`에 모듈 추가
- [ ] `GoogleCalendarPlugin.java` (PF4J 진입점)

### Phase 2: 인증 구현

- [ ] `GoogleAuthService.java` (JSON 키 → Credentials)
- [ ] 파일 경로 방식 인증 구현
- [ ] 토큰 자동 갱신 처리

### Phase 3: API 구현

- [ ] `GoogleCalendarService.java`
- [ ] `calendar.list` 구현
- [ ] `events.list` 구현
- [ ] `events.get` 구현
- [ ] `events.create` 구현
- [ ] `events.update` 구현
- [ ] `events.delete` 구현

### Phase 4: 플러그인 통합

- [ ] `GoogleCalendarPluginExecutor.java` (action 라우팅)
- [ ] ExecuteResponse 표준화
- [ ] 에러 처리 통합

### Phase 5: 테스트

- [ ] 단위 테스트
- [ ] 통합 테스트 (실제 Google API)
- [ ] 테스트 문서 작성

---

## 10. 테스트 환경 설정

### 10.1 사전 준비

1. **GCP 프로젝트 생성**
   - Google Cloud Console 접속
   - 새 프로젝트 생성

2. **Calendar API 활성화**
   - APIs & Services → Enable APIs
   - Google Calendar API 검색 → 활성화

3. **Service Account 생성**
   - IAM & Admin → Service Accounts
   - Create Service Account
   - JSON 키 파일 다운로드

4. **Domain-Wide Delegation 설정**
   - Google Workspace 관리 콘솔
   - 보안 → API 제어 → 도메인 전체 위임
   - Client ID 등록
   - Scope 추가: `https://www.googleapis.com/auth/calendar`

### 10.2 로컬 테스트

```bash
# JSON 키 파일 위치
/Users/mrlhs/dop-global-apps/app/server/config/google-service-account.json

# 테스트 실행
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "google-calendar",
    "action": "calendar.list",
    "connectionId": 1,
    "params": {}
  }'
```

---

## 11. Slack 플러그인과 비교

| 항목 | Slack | Google Calendar |
|------|-------|-----------------|
| **인증 방식** | OAuth 2.0 | Service Account (API Key) |
| **토큰 관리** | access_token + refresh_token | JSON 키 파일 |
| **연동 단위** | Workspace (teamId) | Workspace + User (delegation) |
| **토큰 갱신** | refresh_token 사용 | credentials.refreshIfExpired() |
| **oauth_credential** | 사용 | 사용 안 함 |
| **apikey_credential** | 사용 안 함 | 사용 (JSON 키 경로) |

---

## 12. 변경 이력

| 날짜 | 버전 | 내용 |
|------|------|------|
| 2026-01-22 | 0.1 | 초안 작성 |
