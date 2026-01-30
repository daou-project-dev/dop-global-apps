# Slack Bolt SDK 통합 구현

## 개요

- **목표**: Slack 공식 SDK(Bolt) 도입으로 이벤트/커맨드/인터랙션 처리
- **기반**: Docker PostgreSQL (데이터 영속화)

---

## 의존성

### libs.versions.toml

```toml
[versions]
slack-bolt = "1.44.2"
jasypt = "3.0.5"

[libraries]
slack-bolt = { module = "com.slack.api:bolt", version.ref = "slack-bolt" }
slack-bolt-servlet = { module = "com.slack.api:bolt-servlet", version.ref = "slack-bolt" }
slack-api-client = { module = "com.slack.api:slack-api-client", version.ref = "slack-bolt" }
jasypt-spring-boot-starter = { module = "com.github.ulisesbocchio:jasypt-spring-boot-starter", version.ref = "jasypt" }
```

### 모듈별 의존성

| 모듈 | 의존성 |
|------|--------|
| dop-gapps-core | `jasypt-spring-boot-starter`, `slack-bolt` (compileOnly) |
| dop-gapps-server | `slack-bolt` |
| plugins/slack-plugin | `slack-bolt`, `slack-api-client` |

---

## 모듈 구조

### Core 모듈

```
dop-gapps-core/src/main/java/com/daou/dop/global/apps/core/
├── crypto/
│   ├── JasyptConfig.java           # Jasypt 암호화 설정
│   └── EncryptedStringConverter.java # JPA 필드 암호화 Converter
├── execute/
│   ├── PluginExecutor.java         # 플러그인 실행 인터페이스
│   └── dto/
│       ├── ExecuteRequest.java     # 실행 요청 DTO
│       └── ExecuteResponse.java    # 실행 응답 DTO
└── slack/
    ├── SlackBoltExtension.java     # 플러그인 ExtensionPoint
    ├── SlackTokenProvider.java     # 토큰 조회 인터페이스
    └── dto/
        └── SlackInstallation.java  # 설치 정보 DTO
```

### Server 모듈

```
dop-gapps-server/src/main/java/com/daou/dop/global/apps/server/
├── execute/
│   ├── ExecuteController.java      # /execute API 엔드포인트
│   └── PluginExecutorService.java  # 플러그인 실행 서비스
└── slack/
    ├── SlackProperties.java            # Slack 설정 Properties
    ├── adapter/
    │   ├── SlackBoltAdapter.java       # Bolt App 관리/요청 처리
    │   └── DatabaseInstallationService.java # DB 기반 토큰 관리
    ├── controller/
    │   └── SlackController.java        # HTTP 엔드포인트
    ├── service/
    │   ├── SlackOAuthService.java      # OAuth 처리
    │   ├── SlackTokenService.java      # SlackTokenProvider 구현체
    │   └── SlackWorkspaceService.java  # 워크스페이스 CRUD
    ├── repository/
    │   └── SlackWorkspaceRepository.java
    └── entity/
        ├── SlackWorkspace.java         # 워크스페이스 Entity
        └── WorkspaceStatus.java        # 상태 Enum
```

### Plugin 모듈

```
plugins/slack-plugin/src/main/java/com/daou/dop/global/apps/plugin/slack/
├── SlackPluginExecutor.java        # PluginExecutor 구현체 (API 실행)
├── SlackBoltExtensionImpl.java     # SlackBoltExtension 구현체
└── handler/
    ├── EventHandler.java           # 이벤트 핸들러 (멘션, 메시지)
    ├── CommandHandler.java         # 슬래시 커맨드 (/hello, /help)
    └── InteractionHandler.java     # 버튼/모달 인터랙션
```

---

## API 엔드포인트

### Slack Bolt 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| POST | `/slack/events` | Slack 이벤트 수신 |
| POST | `/slack/commands` | 슬래시 커맨드 처리 |
| POST | `/slack/interactions` | 인터랙션 (버튼, 모달) |
| GET | `/slack/install` | OAuth 설치 시작 |
| GET | `/slack/oauth/callback` | OAuth 콜백 |

### Execute API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/execute` | 플러그인 API 실행 |

**요청 예시**:
```json
{
  "plugin": "slack",
  "method": "POST",
  "uri": "chat.postMessage",
  "teamId": "T0A8Q035DED",
  "body": "{\"channel\":\"C0A89167LHL\",\"text\":\"Hello!\"}"
}
```

---

## 지원 API (SlackPluginExecutor)

| URI | 설명 |
|-----|------|
| `chat.postMessage` | 채널에 메시지 전송 |
| `conversations.list` | Public 채널 목록 조회 |

### chat.postMessage

```bash
curl -k -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "slack",
    "method": "POST",
    "uri": "chat.postMessage",
    "teamId": "TXXXXXX",
    "body": "{\"channel\":\"CXXXXXX\",\"text\":\"메시지 내용\"}"
  }'
```

### conversations.list

```bash
curl -k -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "slack",
    "method": "GET",
    "uri": "conversations.list",
    "teamId": "TXXXXXX",
    "body": "{}"
  }'
```

---

## 설정

### application-secret.yml

```yaml
slack:
  app:
    client-id: "클라이언트ID"
    client-secret: "클라이언트시크릿"
    signing-secret: "서명시크릿"
    scopes: "channels:history,channels:read,chat:write,chat:write.public,commands,app_mentions:read,im:history,im:read,im:write"
    redirect-uri: "https://localhost:8443/slack/oauth/callback"
```

### 환경변수

| 변수명 | 설명 |
|--------|------|
| `SLACK_CLIENT_ID` | Slack App Client ID |
| `SLACK_CLIENT_SECRET` | Slack App Client Secret |
| `SLACK_SIGNING_SECRET` | Slack App Signing Secret |
| `OPS_CONK` | Jasypt 암호화 키 (운영 환경) |

---

## 핵심 구현 상세

### 1. PluginExecutor 인터페이스

```java
public interface PluginExecutor extends ExtensionPoint {
    String getPluginName();
    ExecuteResponse execute(ExecuteRequest request);
}
```

- 플러그인별 API 실행 로직 구현
- Server에서 토큰 주입 후 플러그인에 전달

### 2. SlackPluginExecutor

```java
@Extension
public class SlackPluginExecutor implements PluginExecutor {
    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        return switch (request.uri()) {
            case "chat.postMessage" -> handleChatPostMessage(request);
            case "conversations.list" -> handleConversationsList(request);
            default -> ExecuteResponse.error(400, "Unsupported API");
        };
    }
}
```

### 3. SlackBoltAdapter

- Jakarta Servlet ↔ Slack Bolt Request 변환 처리
- `SlackRequestParser`로 요청 파싱
- 플러그인 핸들러 자동 등록

> **참고**: `bolt-servlet`이 javax.servlet 기반이라 Spring Boot 4.x (jakarta.servlet)와 호환 안됨 → 직접 변환 로직 구현

### 4. Jasypt 암호화

```java
@Convert(converter = EncryptedStringConverter.class)
@Column(name = "access_token")
private String accessToken;  // DB 저장 시 자동 암호화
```

- Local 환경: 키 = "local"
- 운영 환경: `OPS_CONK` 환경변수

### 5. SlackWorkspace Entity

```java
@Entity
public class SlackWorkspace {
    private Long id;
    private String teamId;      // Slack Team ID
    private String teamName;
    private String accessToken; // 암호화 저장
    private String botUserId;
    private String scope;
    private WorkspaceStatus status;
    private Instant installedAt;
}
```

---

## 실행 방법

### 로컬 개발 (Docker PostgreSQL)

```bash
# PostgreSQL 시작
docker-compose up -d

# 서버 실행
./gradlew bootRun

# OAuth 설치
# 브라우저: https://localhost:8443/slack/install
```

---

## 기본 제공 핸들러

### 이벤트

| 이벤트 | 동작 |
|--------|------|
| `app_mention` | "안녕하세요! 무엇을 도와드릴까요?" 응답 |
| `message` | 로깅 (봇 메시지 제외) |

### 슬래시 커맨드

| 커맨드 | 동작 |
|--------|------|
| `/hello [이름]` | 인사 응답 |
| `/help` | 도움말 출력 |

### 인터랙션

| Action ID | 동작 |
|-----------|------|
| `button_click` | "버튼이 클릭되었습니다!" 응답 |
| `approve_action` | "승인되었습니다." 응답 |
| `reject_action` | "거절되었습니다." 응답 |

---

## 추후 작업

- [ ] Redis 캐싱 (토큰, 이벤트 중복 체크)
- [ ] Rate Limiting
- [ ] OAuth State 검증
- [ ] 추가 Slack API 지원 (users.list, files.upload 등)
- [ ] 단위/통합 테스트
