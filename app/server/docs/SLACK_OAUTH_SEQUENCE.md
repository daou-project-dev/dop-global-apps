# Slack OAuth Sequence Diagram

Slack OAuth 연동 과정을 모듈별로 정리한 시퀀스 다이어그램

## Sequence Diagram (현재 구조)

```mermaid
sequenceDiagram
    autonumber

    participant User as 사용자
    participant Browser as 브라우저
    participant Controller as SlackController<br/>(Server)
    participant OAuthService as SlackOAuthService<br/>(Server)
    participant TokenService as SlackTokenService<br/>(Server)
    participant WorkspaceService as SlackWorkspaceService<br/>(Server)
    participant Converter as EncryptedStringConverter<br/>(Core)
    participant SlackAPI as Slack API
    participant DB as Database

    %% OAuth 설치 시작
    rect rgb(230, 245, 255)
        Note over User,DB: OAuth 설치 시작 (Install Flow)
        User->>Browser: Slack 앱 설치 클릭
        Browser->>+Controller: GET /slack/install
        Controller->>+OAuthService: getInstallUrl()
        OAuthService->>OAuthService: UUID state 생성
        OAuthService-->>-Controller: OAuth URL 반환
        Controller-->>-Browser: 302 Redirect
        Browser->>SlackAPI: Slack 인증 페이지 이동
        SlackAPI->>User: 권한 동의 요청
        User->>SlackAPI: 권한 승인
    end

    %% OAuth 콜백 처리
    rect rgb(255, 245, 230)
        Note over User,DB: OAuth 콜백 처리 (Callback Flow)
        SlackAPI-->>Browser: 302 Redirect (code, state)
        Browser->>+Controller: GET /slack/oauth/callback?code=...&state=...
        Controller->>+OAuthService: handleCallback(code, state)
        OAuthService->>OAuthService: state 검증
        OAuthService->>+SlackAPI: oauth.v2.access (code 교환)
        SlackAPI-->>-OAuthService: OAuthV2AccessResponse<br/>(accessToken, teamId, botUserId, scope)
        OAuthService->>OAuthService: SlackInstallation DTO 생성<br/>(Core 모듈)
    end

    %% 토큰 저장
    rect rgb(230, 255, 230)
        Note over User,DB: 토큰 저장 (Storage Flow)
        OAuthService->>+TokenService: save(SlackInstallation)
        TokenService->>+WorkspaceService: saveOrUpdate(SlackWorkspace)
        WorkspaceService->>WorkspaceService: 기존 워크스페이스 확인
        alt 기존 워크스페이스 존재
            WorkspaceService->>WorkspaceService: updateToken()
        else 신규 설치
            WorkspaceService->>WorkspaceService: 새 Entity 생성
        end
        WorkspaceService->>+Converter: convertToDatabaseColumn(accessToken)
        Converter->>Converter: Jasypt 암호화
        Converter-->>-WorkspaceService: 암호화된 토큰
        WorkspaceService->>+DB: INSERT/UPDATE slack_workspace
        DB-->>-WorkspaceService: 저장 완료
        WorkspaceService-->>-TokenService: SlackWorkspace
        TokenService-->>-OAuthService: 저장 완료
        OAuthService-->>-Controller: 처리 완료
        Controller-->>-Browser: 설치 완료 페이지
    end
```

## 모듈별 역할 (현재)

| 모듈 | 컴포넌트 | 역할 |
|------|----------|------|
| **Server** | `SlackController` | REST 엔드포인트 제공 (`/slack/install`, `/slack/oauth/callback`) |
| **Server** | `SlackOAuthService` | OAuth 흐름 처리, Slack API 호출, 토큰 교환 |
| **Server** | `SlackTokenService` | `SlackTokenProvider` 구현, 저장 로직 위임 |
| **Server** | `SlackWorkspaceService` | 워크스페이스 비즈니스 로직 (신규/갱신 판단) |
| **Core** | `SlackInstallation` | 설치 정보 전달용 DTO (Record) |
| **Core** | `SlackTokenProvider` | 토큰 저장/조회 인터페이스 |
| **Core** | `EncryptedStringConverter` | accessToken 암호화/복호화 |

## 주요 파일 위치 (현재)

### Core 모듈
- `dop-global-apps-core/src/main/java/com/daou/dop/global/apps/core/slack/SlackTokenProvider.java`
- `dop-global-apps-core/src/main/java/com/daou/dop/global/apps/core/slack/SlackInstallation.java`
- `dop-global-apps-core/src/main/java/com/daou/dop/global/apps/core/slack/EncryptedStringConverter.java`

### Server 모듈
- `dop-global-apps-server/src/main/java/com/daou/dop/global/apps/server/slack/SlackController.java`
- `dop-global-apps-server/src/main/java/com/daou/dop/global/apps/server/slack/SlackOAuthService.java`
- `dop-global-apps-server/src/main/java/com/daou/dop/global/apps/server/slack/SlackTokenService.java`
- `dop-global-apps-server/src/main/java/com/daou/dop/global/apps/server/slack/SlackWorkspaceService.java`

## 엔드포인트 (현재)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/slack/install` | OAuth 설치 시작 |
| GET | `/slack/oauth/callback` | OAuth 콜백 처리 |

---

## 개선안

개선된 플러그인 아키텍처 설계는 [SLACK_OAUTH_SEQUENCE_V2.md](./SLACK_OAUTH_SEQUENCE_V2.md) 참고
