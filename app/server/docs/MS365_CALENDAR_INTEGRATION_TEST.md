# MS365 Calendar 연동 테스트 가이드

## 사전 준비

### 1. Azure 계정

- [Azure Portal](https://portal.azure.com) 접속 가능한 계정
- Microsoft 365 개인 계정 또는 조직 계정

### 2. 인증 방식

| 방식 | 용도 | 특징 |
|------|------|------|
| **OAuth 2.0 + PKCE** | 개인/조직 계정 | 사용자 동의 필요, 위임된 권한 |

---

## Azure Portal 설정

### 1. 앱 등록 생성

1. [Azure Portal](https://portal.azure.com) → **Microsoft Entra ID** → **App registrations**
2. **New registration** 클릭

| 항목 | 설정값 |
|------|--------|
| Name | `DOP Global Apps - MS365 Calendar` |
| Supported account types | **Accounts in any organizational directory and personal Microsoft accounts** |

> **주의**: 세 번째 옵션 필수 선택 (개인 계정 지원)

### 2. Redirect URI 설정

**Authentication** → **Add a platform** → **Web** 선택

| 환경 | Redirect URI |
|------|--------------|
| 로컬 | `https://localhost:8443/oauth/ms365-calendar/callback` |
| 운영 | `https://your-domain.com/oauth/ms365-calendar/callback` |

> **주의**: SPA가 아닌 **Web** 플랫폼 선택

### 3. Client Secret 생성

**Certificates & secrets** → **New client secret**

| 항목 | 설정값 |
|------|--------|
| Description | `DOP Global Apps` |
| Expires | 24 months (권장) |

**Value** 즉시 복사 (한 번만 표시)

### 4. Manifest 확인

**Manifest** 탭에서 확인:

```json
{
  "accessTokenAcceptedVersion": 2,
  "signInAudience": "AzureADandPersonalMicrosoftAccount"
}
```

> `accessTokenAcceptedVersion`이 `null`이면 `2`로 변경

### 5. API Permissions

**API permissions** → **Add a permission** → **Microsoft Graph** → **Delegated permissions**

| 권한 | 설명 |
|------|------|
| `User.Read` | 프로필 조회 |
| `Calendars.Read` | 캘린더 읽기 |
| `Calendars.ReadWrite` | 캘린더 쓰기 |
| `offline_access` | 리프레시 토큰 |

---

## 로컬 환경 설정

### 1. DB 설정

```sql
INSERT INTO plugin (
    plugin_id, name, auth_type, client_id, client_secret,
    icon_url, active, created_at, updated_at
) VALUES (
    'ms365-calendar',
    'Microsoft 365 Calendar',
    'OAUTH2',
    'YOUR_CLIENT_ID',
    'YOUR_CLIENT_SECRET',
    'https://upload.wikimedia.org/wikipedia/commons/d/df/Microsoft_Office_Outlook_%282018%E2%80%93present%29.svg',
    true, NOW(), NOW()
);
```

### 2. 서버 실행

```bash
cd app/server
./gradlew bootRun
```

### 3. 플러그인 로딩 확인

서버 로그:
```
Registered OAuthHandler: ms365-calendar
Registered PluginExecutor: ms365-calendar (actions: [me.get, calendars.list, events.list, events.create, events.delete])
```

---

## OAuth 인증 테스트

### 1. 브라우저에서 인증 시작

```
https://localhost:8443/oauth/ms365-calendar/install
```

### 2. 인증 플로우

1. Microsoft 로그인 페이지 리다이렉트
2. 개인/조직 계정 선택
3. 권한 동의 (Calendars.Read, Calendars.ReadWrite 등)
4. "Installation successful!" 메시지 확인

### 3. Connection 확인

```sql
SELECT * FROM connection WHERE plugin_id = 'ms365-calendar';
```

---

## 테스트 API

### 1. 내 프로필 조회

```bash
curl -k -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "ms365-calendar",
    "externalId": "YOUR_EXTERNAL_ID",
    "action": "me.get",
    "params": {}
  }'
```

**응답**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": {
    "id": "user-id",
    "displayName": "홍길동",
    "mail": "user@outlook.com"
  }
}
```

### 2. 캘린더 목록 조회

```bash
curl -k -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "ms365-calendar",
    "externalId": "YOUR_EXTERNAL_ID",
    "action": "calendars.list",
    "params": {}
  }'
```

**응답**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": {
    "value": [
      {
        "id": "calendar-id",
        "name": "Calendar",
        "isDefaultCalendar": true
      }
    ]
  }
}
```

### 3. 이벤트 목록 조회

```bash
curl -k -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "ms365-calendar",
    "externalId": "YOUR_EXTERNAL_ID",
    "action": "events.list",
    "params": {
      "top": 10,
      "orderBy": "start/dateTime desc",
      "select": "subject,start,end,location"
    }
  }'
```

### 4. 이벤트 생성

```bash
curl -k -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "ms365-calendar",
    "externalId": "YOUR_EXTERNAL_ID",
    "action": "events.create",
    "params": {
      "subject": "테스트 회의",
      "body": "API로 생성한 일정",
      "startDateTime": "2026-01-27T10:00:00",
      "endDateTime": "2026-01-27T11:00:00",
      "timeZone": "Asia/Seoul",
      "location": "회의실 A"
    }
  }'
```

### 5. 이벤트 삭제

```bash
curl -k -X POST https://localhost:8443/api/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pluginId": "ms365-calendar",
    "externalId": "YOUR_EXTERNAL_ID",
    "action": "events.delete",
    "params": {
      "eventId": "EVENT_ID"
    }
  }'
```

---

## 트러블슈팅

### 1. 개인 계정 로그인 옵션 미표시

**증상**: Microsoft 로그인에서 개인 계정 선택 불가

**원인/해결**:
| 원인 | 해결 |
|------|------|
| Supported account types 잘못 선택 | 새 앱 등록 (세 번째 옵션) |
| `accessTokenAcceptedVersion: null` | Manifest에서 `2`로 변경 |

---

### 2. Public clients can't send a client secret (AADSTS90023)

**증상**: 토큰 교환 시 client_secret 오류

**원인**: SPA 플랫폼으로 설정됨

**해결**:
1. Authentication → SPA redirect URI 삭제
2. Web 플랫폼으로 redirect URI 재등록

---

### 3. The provided request must include a 'client_secret' (AADSTS70002)

**증상**: client_secret 필수 오류

**원인**: Web 플랫폼은 client_secret 필수

**해결**:
1. Azure에서 client secret 생성
2. DB에 client_secret 저장

---

### 4. PKCE required (cross-origin authorization code redemption)

**증상**: PKCE 필수 오류

**원인**: Microsoft가 PKCE 요구

**해결**: 백엔드 PKCE 구현 확인 (이미 적용됨)
- `PluginOAuthController`: code_verifier/code_challenge 생성
- `Ms365CalendarOAuthHandler`: authorization URL 및 token exchange에 PKCE 파라미터 포함

---

### 5. Plugin not found: ms365-calendar

**증상**: 플러그인 미발견

**원인**: 플러그인 미로드

**해결**:
1. `dop-gapps-api/build.gradle` 확인:
   ```groovy
   runtimeOnly project(':plugins:ms365-calendar-plugin')
   ```
2. `settings.gradle` 확인:
   ```groovy
   include 'plugins:ms365-calendar-plugin'
   ```
3. 서버 재시작

---

### 6. Invalid client_id

**증상**: AADSTS700016 오류

**원인**: client_id 오타 또는 미등록

**해결**:
1. Azure Portal에서 Application (client) ID 재확인
2. DB의 client_id 값 검증

---

## 관련 문서

- [plugins/ms365-calendar-plugin/docs/AZURE_APP_REGISTRATION.md](../plugins/ms365-calendar-plugin/docs/AZURE_APP_REGISTRATION.md) - Azure 앱 등록 상세
- [DESIGN/PLUGIN.md](./DESIGN/PLUGIN.md) - 플러그인 아키텍처
