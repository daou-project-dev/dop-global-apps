# Google Calendar 연동 테스트 가이드

## 사전 준비

### 1. GCP 프로젝트 설정

1. [Google Cloud Console](https://console.cloud.google.com) 접속
2. 프로젝트 생성 또는 선택
3. APIs & Services → Enable APIs → **Google Calendar API** 활성화

### 2. 인증 방식 선택

| 방식 | 용도 | 설정 |
|------|------|------|
| **ADC (로컬 테스트)** | 개인 계정으로 빠른 테스트 | `gcloud auth` 사용 |
| **Service Account (운영)** | 고객사별 연동 | JSON 키 파일 발급 |

---

## 방법 1: ADC (Application Default Credentials)

로컬 개발 환경에서 개인 계정으로 테스트.

### 1-1. gcloud CLI 설치

```bash
# macOS
brew install google-cloud-sdk

# 또는 공식 설치
curl https://sdk.cloud.google.com | bash
```

### 1-2. ADC 로그인

```bash
gcloud auth application-default login \
  --scopes="https://www.googleapis.com/auth/calendar,https://www.googleapis.com/auth/cloud-platform"
```

### 1-3. 프로젝트 설정

```bash
# 프로젝트 확인
gcloud config get-value project

# quota 프로젝트 설정 (필요 시)
gcloud auth application-default set-quota-project YOUR_PROJECT_ID
```

### 1-4. JSON 키 위치

```
~/.config/gcloud/application_default_credentials.json
```

---

## 방법 2: Service Account (운영)

### 2-1. Service Account 생성

1. GCP Console → IAM & Admin → Service Accounts
2. Create Service Account
3. 역할: Calendar API 관련 권한 부여
4. Keys → Add Key → Create new key → JSON

### 2-2. Domain-Wide Delegation (Google Workspace)

1. Google Workspace 관리 콘솔 접속
2. 보안 → API 제어 → 도메인 전체 위임
3. Client ID 등록 (Service Account의 Client ID)
4. OAuth 범위 추가:
   ```
   https://www.googleapis.com/auth/calendar
   ```

### 2-3. JSON 키 경로 설정

GoogleAuthService에서 JSON 키 경로 설정:
```java
private static final String LOCAL_JSON_KEY_PATH =
    System.getProperty("user.home") + "/.config/gcloud/application_default_credentials.json";
```

---

## 로컬 환경 설정

### 1. DB 설정

```sql
-- plugin 등록
INSERT INTO plugin (plugin_id, name, description, auth_type, status)
VALUES ('google-calendar', 'Google Calendar', 'Google Workspace Calendar 연동', 'SERVICE_ACCOUNT', 'ACTIVE');
```

### 2. 서버 실행

```bash
cd app/server
./gradlew bootRun
```

### 3. 플러그인 로딩 확인

서버 로그에서 확인:
```
Registered PluginExecutor: google-calendar (actions: [calendar.list, events.list, ...])
Total 2 PluginExecutors registered
```

---

## 테스트 API

### 1. 캘린더 목록 조회

```bash
curl -k -s -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "google-calendar",
    "action": "calendar.list",
    "params": {}
  }'
```

**응답 예시**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": {
    "calendars": [
      {
        "id": "primary",
        "summary": "user@company.com",
        "primary": true,
        "accessRole": "owner",
        "timeZone": "Asia/Seoul"
      }
    ]
  }
}
```

### 2. 일정 목록 조회

```bash
curl -k -s -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "google-calendar",
    "action": "events.list",
    "params": {
      "calendarId": "primary",
      "maxResults": 10,
      "singleEvents": true,
      "orderBy": "startTime"
    }
  }'
```

### 3. 일정 생성

```bash
curl -k -s -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "google-calendar",
    "action": "events.create",
    "params": {
      "calendarId": "primary",
      "event": {
        "summary": "테스트 일정",
        "description": "API로 생성한 일정",
        "start": {
          "dateTime": "2026-01-25T10:00:00+09:00",
          "timeZone": "Asia/Seoul"
        },
        "end": {
          "dateTime": "2026-01-25T11:00:00+09:00",
          "timeZone": "Asia/Seoul"
        }
      }
    }
  }'
```

### 4. 일정 조회

```bash
curl -k -s -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "google-calendar",
    "action": "events.get",
    "params": {
      "calendarId": "primary",
      "eventId": "EVENT_ID"
    }
  }'
```

### 5. 일정 수정

```bash
curl -k -s -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "google-calendar",
    "action": "events.update",
    "params": {
      "calendarId": "primary",
      "eventId": "EVENT_ID",
      "event": {
        "summary": "수정된 일정 제목"
      }
    }
  }'
```

### 6. 일정 삭제

```bash
curl -k -s -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "google-calendar",
    "action": "events.delete",
    "params": {
      "calendarId": "primary",
      "eventId": "EVENT_ID"
    }
  }'
```

---

## 트러블슈팅

### 1. Plugin not found: google-calendar

**증상**:
```json
{"success":false,"statusCode":404,"error":"Plugin not found: google-calendar"}
```

**원인**: 플러그인이 로드되지 않음

**해결**:
1. `dop-global-apps-api/build.gradle`에 의존성 추가:
   ```groovy
   runtimeOnly project(':plugins:google-calendar-plugin')
   ```
2. 서버 재시작

---

### 2. API key (JSON key path) required

**증상**:
```json
{"success":false,"statusCode":401,"error":"API key (JSON key path) required"}
```

**원인**: CredentialContext에 apiKey가 없음

**해결**: 로컬 테스트 시 GoogleAuthService에서 하드코딩된 경로 사용 확인

---

### 3. PKIX path building failed (SSL 에러)

**증상**:
```json
{"error":"IO error: javax.net.ssl.SSLHandshakeException: PKIX path building failed"}
```

**원인**: 회사 프록시 환경에서 Google API 서버 인증서 검증 실패

**해결**:
1. 회사 네트워크 우회 (모바일 핫스팟)
2. 또는 Java cacerts에 회사 인증서 추가:
   ```bash
   sudo keytool -importcert \
     -file /path/to/company.crt \
     -keystore $JAVA_HOME/lib/security/cacerts \
     -alias company-ssl \
     -storepass changeit \
     -noprompt
   ```

---

### 4. Permission denied: Request had insufficient authentication scopes

**증상**:
```json
{"success":false,"statusCode":403,"error":"Permission denied: Request had insufficient authentication scopes"}
```

**원인**: ADC에 Calendar 스코프 없음

**해결**:
```bash
gcloud auth application-default login \
  --scopes="https://www.googleapis.com/auth/calendar,https://www.googleapis.com/auth/cloud-platform"
```

---

### 5. serviceusage.services.use 권한 없음

**증상**:
```
Cannot add the project "xxx" to ADC as a quota project because the account does not have "serviceusage.services.use" permission
```

**원인**: GCP 프로젝트에서 권한 부족

**해결**: 프로젝트 관리자에게 역할 요청
- `Service Usage Consumer` (`roles/serviceusage.serviceUsageConsumer`)

---

## 환경변수 (Java SSL)

회사 프록시 환경에서 Java 앱 실행 시:

```bash
export JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStore=/path/to/truststore.jks -Djavax.net.ssl.trustStorePassword=changeit"
./gradlew bootRun
```

---

## 관련 문서

- [GOOGLE_CALENDAR_PLUGIN_PLAN.md](./GOOGLE_CALENDAR_PLUGIN_PLAN.md) - 구현 계획
- [GOOGLE_CALENDAR_PLUGIN_IMPLEMENTATION.md](./GOOGLE_CALENDAR_PLUGIN_IMPLEMENTATION.md) - 구현 상세
