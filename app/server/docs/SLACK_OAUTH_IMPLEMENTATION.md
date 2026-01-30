# Slack OAuth 구현

## 개요

Slack 앱 설치를 위한 OAuth 2.0 플로우 구현

---

## 구현 파일

### 1. SlackProperties (설정)

**경로**: `dop-gapps-server/.../slack/SlackProperties.java`

```java
@ConfigurationProperties(prefix = "slack.app")
public record SlackProperties(
        String clientId,
        String clientSecret,
        String signingSecret,
        String scopes,
        String redirectUri
) {}
```

### 2. SlackOAuthService (OAuth 처리)

**경로**: `dop-gapps-server/.../slack/service/SlackOAuthService.java`

**주요 기능**:
- `getInstallUrl()`: OAuth 설치 URL 생성 (redirect_uri 포함)
- `handleCallback()`: OAuth 콜백 처리 → 토큰 교환 → DB 저장

### 3. SlackController (HTTP 엔드포인트)

**경로**: `dop-gapps-server/.../slack/controller/SlackController.java`

| 엔드포인트 | 설명 |
|-----------|------|
| `GET /slack/install` | OAuth 설치 페이지로 리다이렉트 |
| `GET /slack/oauth/callback` | OAuth 콜백 처리 |

### 4. SlackWorkspace Entity (토큰 저장)

**경로**: `dop-gapps-server/.../slack/entity/SlackWorkspace.java`

| 필드 | 설명 |
|------|------|
| teamId | Slack 워크스페이스 ID |
| teamName | 워크스페이스 이름 |
| accessToken | Bot OAuth Token (암호화 저장) |
| botUserId | Bot User ID |
| status | ACTIVE / REVOKED |

### 5. SlackTokenService (토큰 관리)

**경로**: `dop-gapps-server/.../slack/service/SlackTokenService.java`

- 토큰 저장/조회
- Jasypt 암호화 적용

---

## 설정 파일

### application-secret.yml (gitignore 포함)

```yaml
slack:
  app:
    client-id: "클라이언트ID"
    client-secret: "클라이언트시크릿"
    signing-secret: "서명시크릿"
    scopes: "channels:history,channels:read,chat:write,chat:write.public,commands,app_mentions:read,im:history,im:read,im:write"
    redirect-uri: "https://localhost:8443/slack/oauth/callback"
```

> **주의**: YAML에서 숫자처럼 보이는 값(예: `123.456`)은 반드시 따옴표로 감싸야 함

### Bot Token Scopes 설명

| Scope | 설명 |
|-------|------|
| `channels:history` | 채널 메시지 기록 읽기 |
| `channels:read` | 채널 정보 조회 |
| `chat:write` | 초대된 채널에 메시지 전송 |
| `chat:write.public` | **초대 없이** public 채널에 메시지 전송 |
| `commands` | 슬래시 커맨드 사용 |
| `app_mentions:read` | 앱 멘션 이벤트 수신 |
| `im:history` | DM 메시지 기록 읽기 |
| `im:read` | DM 정보 조회 |
| `im:write` | DM 메시지 전송 |

### application-local.yml (로컬 HTTPS)

```yaml
jasypt:
  encryptor:
    password: local

server:
  port: 8443
  ssl:
    key-store: classpath:localhost.p12
    key-store-password: changeit
    key-store-type: PKCS12
```

---

## HTTPS 설정

### Self-signed 인증서 생성

```bash
cd dop-gapps-server/src/main/resources

keytool -genkeypair -alias localhost \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore localhost.p12 \
  -validity 3650 \
  -storepass changeit \
  -dname "CN=localhost, OU=Dev, O=Daou, L=Seoul, ST=Seoul, C=KR" \
  -ext "SAN=DNS:localhost,IP:127.0.0.1"
```

> Slack OAuth는 HTTPS redirect URI만 허용

---

## Jasypt 암호화

### JasyptConfig

**경로**: `dop-gapps-core/.../crypto/JasyptConfig.java`

- Local 환경: 암호화 키 = "local"
- 운영 환경: 환경변수 `OPS_CONK`에서 키 로드

### EncryptedStringConverter

**경로**: `dop-gapps-core/.../crypto/EncryptedStringConverter.java`

- JPA Entity 필드 자동 암호화/복호화
- SlackWorkspace.accessToken에 적용

---

## OAuth 플로우

```
┌──────────┐      ┌──────────┐      ┌──────────┐
│  Browser │      │  Server  │      │  Slack   │
└────┬─────┘      └────┬─────┘      └────┬─────┘
     │ GET /slack/install                │
     │ ───────────────► │                │
     │                  │                │
     │ ◄─ 302 Redirect ─┤                │
     │    to Slack OAuth│                │
     │                                   │
     │ ──────────────────────────────────►
     │         Slack 인증 페이지         │
     │ ◄──────────────────────────────────
     │                                   │
     │ GET /slack/oauth/callback?code=.. │
     │ ───────────────► │                │
     │                  │ oauth.v2.access│
     │                  │ ───────────────►
     │                  │ ◄── 토큰 응답 ──
     │                  │                │
     │                  │ DB 저장        │
     │                  │                │
     │ ◄── 설치 완료 ───┤                │
     │                  │                │
```

---

## OAuth 콜백 데이터

### 1. 콜백 파라미터

`GET /slack/oauth/callback?code=...&state=...`

| 파라미터 | 예시 | 설명 |
|---------|------|------|
| `code` | `10296003183489.10343521196929.653c1e3f...` | 임시 인증 코드 |
| `state` | `6c72e25d-3744-476e-b57c-72d9a2e3b9a8` | CSRF 방지용 상태값 |

### 2. oauth.v2.access 응답

```json
{
  "ok": true,
  "app_id": "A0A8B25JCTU",
  "authed_user": {
    "id": "U0A84LHANRH"
  },
  "scope": "channels:history,channels:read,chat:write,chat:write.public,...",
  "token_type": "bot",
  "access_token": "xoxb-...",
  "bot_user_id": "U0AA3GAUSSD",
  "team": {
    "id": "T0A8Q035DED",
    "name": "DO TEST"
  },
  "enterprise": null,
  "is_enterprise_install": false
}
```

### 3. DB 저장 데이터

| 필드 | 값 |
|------|-----|
| teamId | `T0A8Q035DED` |
| teamName | `DO TEST` |
| accessToken | `xoxb-...` (암호화 저장) |
| botUserId | `U0AA3GAUSSD` |
| scope | `channels:history,channels:read,...` |
| status | `ACTIVE` |
| installedAt | 설치 시각 |

---

## 관련 문서

- [SLACK_BOLT_INTEGRATION_PLAN.md](./SLACK_BOLT_INTEGRATION_PLAN.md) - 전체 통합 계획
- [SLACK_PLUGIN_IMPLEMENTATION.md](./SLACK_PLUGIN_IMPLEMENTATION.md) - 플러그인 구현
- [SLACK_INTEGRATION_TEST.md](./SLACK_INTEGRATION_TEST.md) - 테스트 가이드
