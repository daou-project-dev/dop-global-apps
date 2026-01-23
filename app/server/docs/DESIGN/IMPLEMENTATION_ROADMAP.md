# 구현 로드맵

## 개요

설계 문서(DOMAIN.md, BACKEND_LAYER.md, PLUGIN.md) 기반 구현 현황 및 로드맵

---

## 1. 현재 구현 완료 항목

### 1.1 모듈 구조

```
server/
├── dop-global-apps-core/        ✅ 생성됨 (V1 인터페이스)
├── dop-global-apps-server/      ✅ 생성됨 (V2 통합 완료)
└── plugins/
    ├── plugin-sdk/              ✅ 신규 (V2 인터페이스/DTO)
    └── slack-plugin/            ✅ V2 구현 완료
```

### 1.2 Core 모듈

| 파일 | 상태 | 비고 |
|------|------|------|
| `PluginExecutor.java` | ✅ V1 | action 기반 아님 (method/uri 방식) |
| `ExecuteRequest.java` | ✅ V1 | Slack 전용 필드 포함 (teamId) |
| `ExecuteResponse.java` | ✅ | 완료 |
| `OAuthHandler.java` | ✅ V1 | PluginConfig 미주입 |
| `OAuthException.java` | ✅ | 완료 |
| `TokenInfo.java` | ✅ | 완료 |
| `TokenStorage.java` | ✅ | 인터페이스 |
| `StateStorage.java` | ✅ | 인터페이스 |
| `EncryptedStringConverter.java` | ✅ | JPA Converter |
| `JasyptConfig.java` | ✅ | 암호화 설정 |

### 1.3 Server 모듈

| 파일 | 상태 | 비고 |
|------|------|------|
| `PluginToken.java` | ✅ | Entity (oauth_credential 단순화) |
| `TokenStatus.java` | ✅ | Enum |
| `PluginTokenRepository.java` | ✅ | JPA Repository |
| `JpaTokenStorage.java` | ✅ | TokenStorage 구현 |
| `InMemoryStateStorage.java` | ✅ | StateStorage 구현 (추후 Redis) |
| `PluginOAuthController.java` | ✅ V2 | OAuth 엔드포인트 (V1 폴백 지원) |
| `ExecuteController.java` | ✅ | API 실행 엔드포인트 |
| `PluginExecutorService.java` | ✅ | 플러그인 실행 서비스 |
| `PluginRegistry.java` | ✅ V2 | V2/V1 확장점 관리 |
| `PluginConfig.java` | ✅ | PF4J 설정 |
| `RestClientConfig.java` | ✅ | HTTP 클라이언트 |
| `VirtualThreadsConfig.java` | ✅ | Virtual Threads 설정 |
| `Plugin.java` | ✅ 신규 | 플러그인 마스터 Entity |
| `PluginRepository.java` | ✅ 신규 | JPA Repository |
| `PluginService.java` | ✅ 신규 | Entity → PluginConfig 변환 |
| `PluginDataInitializer.java` | ✅ 신규 | 초기 데이터 설정 |
| `PluginConnection.java` | ✅ 신규 | 플러그인 연동 Entity |
| `OAuthCredential.java` | ✅ 신규 | OAuth 인증 정보 Entity |
| `ConnectionService.java` | ✅ 신규 | 연동 관리 서비스 |
| `PluginConnectionRepository.java` | ✅ 신규 | JPA Repository |
| `OAuthCredentialRepository.java` | ✅ 신규 | JPA Repository |
| `Company.java` | ✅ 신규 | 고객사 Entity |
| `CompanyRepository.java` | ✅ 신규 | JPA Repository |
| `User.java` | ✅ 신규 | 사용자 Entity |
| `UserRepository.java` | ✅ 신규 | JPA Repository |

### 1.4 plugin-sdk 모듈 (신규)

| 파일 | 상태 | 비고 |
|------|------|------|
| `PluginConfig.java` | ✅ | 서버→플러그인 설정 DTO |
| `CredentialContext.java` | ✅ | 서버→플러그인 인증 DTO |
| `ExecuteRequest.java` | ✅ | V2 실행 요청 (action 기반) |
| `ExecuteResponse.java` | ✅ | 실행 응답 |
| `TokenInfo.java` | ✅ | 플러그인→서버 토큰 DTO |
| `OAuthHandler.java` | ✅ | V2 OAuth 인터페이스 |
| `PluginExecutor.java` | ✅ | V2 실행 인터페이스 |
| `OAuthException.java` | ✅ | OAuth 예외 |

### 1.5 Slack Plugin

| 파일 | 상태 | 비고 |
|------|------|------|
| `SlackPlugin.java` | ✅ | PF4J Plugin 진입점 |
| `SlackExtension.java` | ✅ | SimpleExtension 구현 |
| `SlackOAuthHandler.java` | ✅ V1 | properties 직접 로드 (Legacy) |
| `SlackOAuthHandlerV2.java` | ✅ 신규 | PluginConfig 주입 방식 |
| `SlackPluginExecutor.java` | ✅ V1 | method/uri 방식 (Legacy) |
| `SlackPluginExecutorV2.java` | ✅ 신규 | action 기반, CredentialContext |
| `EventHandler.java` | ⬜ | 스켈레톤만 |
| `CommandHandler.java` | ⬜ | 스켈레톤만 |
| `InteractionHandler.java` | ⬜ | 스켈레톤만 |

### 1.5 API 엔드포인트

| Method | Endpoint | 상태 | 비고 |
|--------|----------|------|------|
| GET | `/oauth/{pluginId}/install` | ✅ | OAuth 시작 |
| GET | `/oauth/{pluginId}/callback` | ✅ | OAuth 콜백 |
| POST | `/api/execute` | ✅ | API 실행 |

---

## 2. 미구현 항목

### 2.1 모듈 (BACKEND_LAYER.md)

| 모듈 | 상태 | 우선순위 |
|------|------|---------|
| `plugins/plugin-sdk` | ✅ 완료 | - |
| `dop-global-apps-domain` | ✅ 완료 | - |
| `dop-global-apps-infrastructure` | ✅ 완료 | - |
| `dop-global-apps-api` | ⚠️ server 모듈로 유지 | P3 |

### 2.2 테이블 (DOMAIN.md)

| 테이블 | 상태 | 우선순위 |
|--------|------|---------|
| `plugin` | ✅ 완료 | - |
| `company` | ✅ 완료 | - |
| `user` | ✅ 완료 | - |
| `plugin_connection` | ✅ 완료 | - |
| `oauth_credential` | ✅ 완료 | - |
| `apikey_credential` | ✅ 완료 | - |

### 2.3 DTO (PLUGIN.md)

| DTO | 상태 | 우선순위 |
|-----|------|---------|
| `PluginConfig` | ✅ 완료 | - |
| `CredentialContext` | ✅ 완료 | - |
| `ExecuteRequest` V2 | ✅ 완료 | - |

### 2.4 인터페이스 (PLUGIN.md)

| 인터페이스 | 상태 | 우선순위 |
|-----------|------|---------|
| `OAuthHandler` V2 | ✅ 완료 | - |
| `PluginExecutor` V2 | ✅ 완료 | - |

### 2.5 기능 (DOMAIN.md 1.3)

| 기능 | 상태 | 우선순위 |
|------|------|---------|
| API 호출 (Outbound) | ✅ | - |
| 이벤트 수신 (Inbound) | ❌ | P3 |
| 토큰 자동 갱신 | ❌ | P2 |
| 연동 해제/재연동 | ❌ | P2 |
| 관리 UI | ❌ | P3 |

---

## 3. 구현 우선순위

### Phase 0: 기반 작업 (✅ 완료)

목표: V2 아키텍처 전환을 위한 최소 기반 구축

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 0: 기반 작업 (완료)                                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. ✅ plugin 테이블 추가                                    │
│     - client_id, client_secret, secrets, metadata 관리      │
│     - PluginDataInitializer로 Slack 초기 데이터 설정         │
│                                                             │
│  2. ✅ plugin-sdk 모듈 분리                                  │
│     - PluginConfig, CredentialContext DTO 추가              │
│     - OAuthHandler V2, PluginExecutor V2 인터페이스          │
│                                                             │
│  3. ✅ SlackOAuthHandlerV2, SlackPluginExecutorV2 구현       │
│     - PluginConfig 주입 방식                                │
│     - action 기반 API 실행                                   │
│                                                             │
│  4. ✅ PluginOAuthController V2 통합                         │
│     - V2 우선 사용, V1 폴백 지원                             │
│                                                             │
│  5. ✅ plugin_connection + oauth_credential 테이블            │
│     - ConnectionService 연동 완료                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**완료된 작업**:
1. ✅ `plugin` 테이블 Entity 추가
2. ✅ `plugin-sdk` 모듈 생성 + DTO/인터페이스 V2
3. ✅ `SlackOAuthHandlerV2` 구현 (PluginConfig 주입)
4. ✅ `SlackPluginExecutorV2` 구현 (action 기반)
5. ✅ `PluginOAuthController` V2 통합 (V1 폴백 지원)
6. ✅ `PluginDataInitializer` 초기 데이터 설정

**다음 단계 (Phase 2)**:
- 모듈 분리 (domain, infrastructure, api)
- apikey_credential 테이블 추가
- 토큰 자동 갱신 구현

### Phase 1: 도메인 확장 (✅ 완료)

목표: 다중 고객사/사용자 지원

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 1: 도메인 확장 (완료)                                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. ✅ plugin_connection 테이블 추가                         │
│     - PluginConnection Entity                               │
│     - PluginConnectionRepository                            │
│                                                             │
│  2. ✅ oauth_credential 테이블 추가                          │
│     - OAuthCredential Entity (암호화 저장)                  │
│     - OAuthCredentialRepository                             │
│                                                             │
│  3. ✅ ConnectionService 구현                                │
│     - saveOAuthToken() - 연동 생성/업데이트                  │
│     - getCredentialContext() - 인증 정보 조회               │
│     - revokeConnection() - 연동 해제                        │
│                                                             │
│  4. ✅ PluginOAuthController ConnectionService 통합          │
│     - V2 OAuth 콜백에서 ConnectionService 사용              │
│                                                             │
│  5. ✅ company, user 테이블 추가                             │
│     - Company Entity (companyUid 프로비저닝)                │
│     - User Entity (platformUserId 프로비저닝)               │
│     - CompanyRepository, UserRepository                     │
│                                                             │
│  6. ✅ slack.properties 제거                                 │
│     - V1 핸들러 deprecated 처리                             │
│     - V2만 @Extension 등록                                  │
│     - DB Single Source of Truth 완료                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Phase 2: 모듈 분리 (✅ 완료)

목표: 클린 아키텍처 적용

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 2: 모듈 분리 (완료)                                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. ✅ domain 모듈 생성                                      │
│     - Entity: Plugin, Company, User, PluginConnection       │
│     - Entity: OAuthCredential, ApiKeyCredential             │
│     - Enum: AuthType, PluginStatus, ScopeType 등            │
│     - Repository 인터페이스 (순수 Java)                      │
│                                                             │
│  2. ✅ infrastructure 모듈 생성                              │
│     - JPA Repository 구현체                                  │
│     - EncryptedStringConverter                              │
│     - JasyptConfig                                          │
│                                                             │
│  3. ✅ server 모듈에 domain/infrastructure 통합              │
│     - runtimeOnly로 infrastructure 의존                     │
│     - 기존 server 코드 호환성 유지                           │
│                                                             │
│  4. ✅ apikey_credential 테이블 추가                         │
│     - ApiKeyCredential Entity                               │
│     - ApiKeyCredentialRepository                            │
│                                                             │
│  5. ⬜ server 코드 domain 모듈로 마이그레이션 (Phase 3)      │
│     - 중복 Entity 제거                                       │
│     - Service에서 domain Repository 사용                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Phase 3: 의존성 구조 정리 (⬜ 예정)

목표: core가 plugin-sdk를 포함하여 진입점 모듈의 의존성 단순화

**문제점**:
```
현재 구조:
api ──────┬──▶ core
          └──▶ plugin-sdk  ← 직접 의존

만약 batch 모듈 추가 시:
api ──────┬──▶ core
          └──▶ plugin-sdk  ← 중복!
batch ────┬──▶ core
          └──▶ plugin-sdk  ← 중복!
```

**목표 구조**:
```
api ──────┐
batch ────┼──▶ core ──┬──▶ plugin-sdk (api dependency)
xxx ──────┘           └──▶ domain
```
- 모든 진입점(api, batch 등)은 core만 의존
- core가 plugin-sdk를 `api` dependency로 노출
- 플러그인 관리/실행 로직은 core에 위치

**이동 대상**:

| 파일 | 현재 위치 | 이동 위치 | 이유 |
|------|----------|----------|------|
| `PluginRegistry.java` | api/plugin/ | core/plugin/ | 플러그인 확장점 관리는 공통 기능 |
| `PluginExecutorService.java` (핵심 로직) | api/execute/ | core/execute/ | 플러그인 실행 로직 재사용 |

**api에 남는 것**:
- `ExecuteController.java` - HTTP 진입점
- `PluginOAuthController.java` - OAuth HTTP 진입점
- api 특화 서비스 로직

**build.gradle 변경**:
```groovy
// core/build.gradle
dependencies {
    api project(':plugins:plugin-sdk')  // 추가
    api project(':dop-global-apps-domain')
}

// api/build.gradle
dependencies {
    implementation project(':dop-global-apps-core')
    // plugin-sdk 제거 (core에서 전이됨)
    runtimeOnly project(':dop-global-apps-infrastructure')
    runtimeOnly project(':plugins:slack-plugin')
}
```

### Phase 4: 고급 기능

목표: 운영 안정성 및 확장

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 4: 고급 기능                                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 토큰 자동 갱신 (Scheduler)                               │
│  2. Redis StateStorage 구현                                  │
│  3. 이벤트 수신 (Slack Events API)                           │
│  4. 연동 해제/재연동 API                                     │
│  5. 관리 UI (Frontend)                                       │
│  6. Google/MS365 플러그인 추가                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Phase 0 상세 작업 목록

### 4.1 plugin 테이블 추가

```java
// Entity: Plugin.java
@Entity
@Table(name = "plugin")
public class Plugin {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String pluginId;           // "slack"

    private String name;               // "Slack"
    private String description;

    @Enumerated(EnumType.STRING)
    private AuthType authType;         // OAUTH2, API_KEY

    private String clientId;

    @Convert(converter = EncryptedStringConverter.class)
    private String clientSecret;

    @Convert(converter = EncryptedJsonConverter.class)
    private String secrets;            // JSON

    @Column(columnDefinition = "TEXT")
    private String metadata;           // JSON

    @Enumerated(EnumType.STRING)
    private PluginStatus status;
}
```

### 4.2 plugin-sdk 모듈 구조

```
plugins/plugin-sdk/
├── build.gradle
└── src/main/java/com/daou/dop/global/apps/plugin/sdk/
    ├── PluginExecutor.java       # V2 인터페이스
    ├── OAuthHandler.java         # V2 인터페이스
    ├── OAuthException.java
    ├── PluginConfig.java         # 신규 DTO
    ├── CredentialContext.java    # 신규 DTO
    ├── ExecuteRequest.java       # V2 DTO
    ├── ExecuteResponse.java
    └── TokenInfo.java
```

### 4.3 마이그레이션 전략

1. **V1/V2 공존 기간**
   - `OAuthHandler` (V1) 유지
   - `OAuthHandlerV2` 추가
   - Server에서 V2 우선 탐색, 없으면 V1 사용

2. **Slack 플러그인 전환**
   - `SlackOAuthHandlerV2` 구현
   - `slack.properties` → DB `plugin` 테이블
   - 테스트 후 V1 deprecated

3. **테이블 마이그레이션**
   - `plugin_token` → `plugin_connection` + `oauth_credential`
   - Flyway 마이그레이션 스크립트

---

## 5. 의존성 변경 계획

### 현재

```
slack-plugin ──▶ core
server ──▶ core
```

### Phase 0 이후

```
slack-plugin ──▶ plugin-sdk
server ──▶ plugin-sdk
server ──▶ core (내부 유틸)
```

### Phase 2 이후 (현재)

```
slack-plugin ──▶ plugin-sdk

api ──▶ core
api ──▶ plugin-sdk  ← 문제: 직접 의존
api ──runtimeOnly──▶ infrastructure
api ──runtimeOnly──▶ slack-plugin

core ──▶ domain
infrastructure ──▶ core
```

### Phase 3 이후 (목표)

```
slack-plugin ──▶ plugin-sdk

api ──▶ core (core가 plugin-sdk 포함)
api ──runtimeOnly──▶ infrastructure
api ──runtimeOnly──▶ slack-plugin

core ──api──▶ plugin-sdk
core ──api──▶ domain
infrastructure ──▶ core
```

---

## 6. 변경 이력

| 날짜 | 버전 | 내용 |
|------|------|------|
| 2025-01-21 | 0.1 | 초안 작성 - 현황 분석 및 로드맵 정의 |
| 2025-01-21 | 0.2 | Phase 0 완료 - plugin-sdk, Plugin Entity, V2 인터페이스 구현 |
| 2025-01-21 | 0.3 | Phase 1 진행 - plugin_connection, oauth_credential, ConnectionService 구현 |
| 2025-01-21 | 0.4 | Phase 1 완료 - company, user Entity, slack.properties 제거, V1 deprecated |
| 2025-01-21 | 0.5 | Phase 2 완료 - domain/infrastructure 모듈 분리, apikey_credential 추가 |
| 2026-01-21 | 0.6 | Phase 3 추가 - 의존성 구조 정리 (core가 plugin-sdk 포함, 플러그인 로직 core 이동) |
