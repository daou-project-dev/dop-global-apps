# V2 OAuth 플러그인 아키텍처 구현

## 개요

기존 Slack 전용 OAuth 구조를 범용 플러그인 아키텍처로 개선
- State 저장/검증 추가 (CSRF 방지)
- 플러그인별 OAuth 처리 분리
- 확장 가능한 구조 (Teams, Discord 등)

---

## 변경 사항

### 생성된 파일

#### Core 모듈 (5개)

| 파일 | 설명 |
|------|------|
| `core/oauth/OAuthHandler.java` | OAuth 처리 ExtensionPoint |
| `core/oauth/StateStorage.java` | State 저장소 인터페이스 |
| `core/oauth/TokenStorage.java` | 토큰 저장소 인터페이스 |
| `core/oauth/TokenInfo.java` | 토큰 정보 DTO |
| `core/oauth/OAuthException.java` | OAuth 예외 |

#### Server 모듈 (7개)

| 파일 | 설명 |
|------|------|
| `server/oauth/InMemoryStateStorage.java` | State 저장소 구현 (메모리) |
| `server/oauth/JpaTokenStorage.java` | 토큰 저장소 구현 (JPA) |
| `server/oauth/entity/PluginToken.java` | 토큰 Entity |
| `server/oauth/entity/TokenStatus.java` | 상태 Enum (ACTIVE, REVOKED, EXPIRED) |
| `server/oauth/repository/PluginTokenRepository.java` | JPA Repository |
| `server/oauth/controller/PluginOAuthController.java` | 범용 OAuth 컨트롤러 |
| `server/plugin/PluginRegistry.java` | 플러그인 레지스트리 |

#### Plugin 모듈 (3개)

| 파일 | 설명 |
|------|------|
| `plugin/slack/SlackOAuthHandler.java` | Slack OAuth 구현 |
| `plugin/slack/SlackEventHandler.java` | Slack 이벤트 핸들러 |
| `plugin/slack/resources/slack.properties` | Slack 설정 파일 |

### 삭제된 파일

#### Core 모듈
- `core/slack/SlackTokenProvider.java`
- `core/slack/SlackBoltExtension.java`
- `core/slack/dto/SlackInstallation.java`

#### Server 모듈
- `server/slack/` 패키지 전체
  - `SlackController.java`
  - `SlackOAuthService.java`
  - `SlackBoltAdapter.java`
  - `SlackTokenService.java`
  - `SlackWorkspaceService.java`
  - `DatabaseInstallationService.java`
  - `SlackWorkspace.java`
  - `SlackProperties.java`
  - `SlackWorkspaceRepository.java`
  - `WorkspaceStatus.java`

#### Plugin 모듈
- `plugin/slack/SlackBoltExtensionImpl.java`

### 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `server/execute/PluginExecutorService.java` | `SlackTokenProvider` → `TokenStorage` 사용 |

---

## 핵심 인터페이스

### OAuthHandler (Plugin 구현)

```java
public interface OAuthHandler extends ExtensionPoint {
    String getPluginId();
    String buildAuthorizationUrl(String state, String redirectUri);
    TokenInfo exchangeCode(String code, String redirectUri) throws OAuthException;
}
```

### StateStorage (Server 구현)

```java
public interface StateStorage {
    String generateAndStore(String pluginId, Duration ttl);
    boolean validateAndConsume(String pluginId, String state);
}
```

### TokenStorage (Server 구현)

```java
public interface TokenStorage {
    void save(TokenInfo tokenInfo);
    Optional<TokenInfo> findByExternalId(String pluginId, String externalId);
    List<TokenInfo> findAllByPluginId(String pluginId);
    void revoke(String pluginId, String externalId);
}
```

---

## 엔드포인트

### 변경 전

| Method | Path | 설명 |
|--------|------|------|
| GET | `/slack/install` | Slack OAuth 시작 |
| GET | `/slack/oauth/callback` | Slack OAuth 콜백 |
| POST | `/slack/events` | Slack 이벤트 수신 |

### 변경 후

| Method | Path | 설명 |
|--------|------|------|
| GET | `/oauth/{plugin}/install` | OAuth 시작 (범용) |
| GET | `/oauth/{plugin}/callback` | OAuth 콜백 (범용) |
| POST | `/plugins/{plugin}/events` | 이벤트 수신 (향후 구현) |

---

## 데이터베이스 스키마

### 변경 전: `slack_workspace`

```sql
CREATE TABLE slack_workspace (
    id BIGINT PRIMARY KEY,
    team_id VARCHAR(255) UNIQUE,
    team_name VARCHAR(255),
    access_token VARCHAR(512),
    bot_user_id VARCHAR(255),
    scope VARCHAR(1000),
    status VARCHAR(50),
    installed_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### 변경 후: `plugin_token`

```sql
CREATE TABLE plugin_token (
    id BIGINT PRIMARY KEY,
    plugin_id VARCHAR(255) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    external_name VARCHAR(255) NOT NULL,
    access_token VARCHAR(1024) NOT NULL,
    refresh_token VARCHAR(1024),
    scope VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP,
    installed_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(plugin_id, external_id)
);

CREATE TABLE plugin_token_metadata (
    token_id BIGINT REFERENCES plugin_token(id),
    meta_key VARCHAR(255),
    meta_value VARCHAR(1000)
);
```

---

## 설정

### Slack 플러그인 설정

환경변수 또는 `slack.properties` 파일로 설정

```properties
# 환경변수 (우선순위 높음)
SLACK_CLIENTID=xxx
SLACK_CLIENTSECRET=xxx
SLACK_SIGNINGSECRET=xxx
SLACK_SCOPES=commands,chat:write,app_mentions:read

# slack.properties (기본값)
slack.clientId=${SLACK_CLIENTID:}
slack.clientSecret=${SLACK_CLIENTSECRET:}
slack.signingSecret=${SLACK_SIGNINGSECRET:}
slack.scopes=commands,chat:write,app_mentions:read,channels:history,channels:read
```

---

## OAuth 흐름

```
┌─────────┐     ┌──────────────────────┐     ┌───────────────┐
│  User   │     │  PluginOAuthController │     │ OAuthHandler  │
└────┬────┘     └──────────┬───────────┘     └───────┬───────┘
     │                     │                         │
     │ GET /oauth/slack/install                      │
     │────────────────────>│                         │
     │                     │                         │
     │                     │ generateAndStore(state) │
     │                     │────────────────────────>│
     │                     │<────────────────────────│
     │                     │                         │
     │                     │ buildAuthorizationUrl() │
     │                     │────────────────────────>│
     │                     │<────────────────────────│
     │                     │                         │
     │ 302 Redirect to Slack                         │
     │<────────────────────│                         │
     │                     │                         │
     │ (Slack 인증 후)      │                         │
     │                     │                         │
     │ GET /oauth/slack/callback?code=xxx&state=yyy  │
     │────────────────────>│                         │
     │                     │                         │
     │                     │ validateAndConsume()    │
     │                     │────────────────────────>│
     │                     │<────────────────────────│
     │                     │                         │
     │                     │ exchangeCode()          │
     │                     │────────────────────────>│
     │                     │<────────────────────────│
     │                     │                         │
     │                     │ tokenStorage.save()     │
     │                     │─────────┐               │
     │                     │<────────┘               │
     │                     │                         │
     │ 200 OK              │                         │
     │<────────────────────│                         │
     │                     │                         │
```

---

## 새 플러그인 추가 방법

1. `OAuthHandler` 구현 클래스 생성
2. `@Extension` 어노테이션 추가
3. `getPluginId()` 반환값 정의
4. `buildAuthorizationUrl()`, `exchangeCode()` 구현

```java
@Extension
public class TeamsOAuthHandler implements OAuthHandler {

    @Override
    public String getPluginId() {
        return "teams";
    }

    @Override
    public String buildAuthorizationUrl(String state, String redirectUri) {
        // Microsoft OAuth URL 생성
    }

    @Override
    public TokenInfo exchangeCode(String code, String redirectUri) throws OAuthException {
        // Microsoft Graph API로 토큰 교환
    }
}
```

---

## 검증 방법

1. 빌드 테스트: `./gradlew clean build`
2. 로컬 실행: `./gradlew bootRun`
3. OAuth 테스트: `/oauth/slack/install` 접속 → Slack 인증 → 콜백 확인
4. State 검증: 잘못된 state로 콜백 → "Invalid state" 에러 확인
5. DB 확인: `plugin_token` 테이블에 토큰 저장 확인
