# Slack Bolt SDK + Redis 통합 구현 계획

## 개요

- **목표**: Slack 공식 SDK(Bolt) 도입 + Redis 활용 성능 최적화
- **범위**: OAuth, 이벤트, 커맨드, 인터랙션 처리 + 캐싱/멱등성/Rate Limiting

---

## 1. 의존성 추가

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

### 모듈별 build.gradle
- **dop-global-apps-core**: `jasypt-spring-boot-starter` (암호화 공통 모듈)
- **dop-global-apps-server**: `spring-boot-starter-data-redis`, `slack-bolt-servlet`
- **plugins/slack-plugin**: `slack-bolt`, `slack-api-client`

---

## 2. 모듈 역할 분담

```
┌─────────────────────────────────────────────────────────────┐
│  dop-global-apps-server                                     │
│  - HTTP 엔드포인트 (SlackController)                        │
│  - Redis 설정/유틸리티                                       │
│  - Entity/Repository (SlackWorkspace)                       │
│  - SlackBoltAdapter (Bolt App 관리)                         │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────┴───────────────────────────────┐
│  dop-global-apps-core                                       │
│  - SlackBoltExtension (ExtensionPoint 인터페이스)           │
│  - SlackTokenProvider (토큰 조회 인터페이스)                 │
│  - DTO 클래스                                               │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────┴───────────────────────────────┐
│  plugins/slack-plugin                                       │
│  - SlackBoltExtensionImpl (핸들러 등록)                      │
│  - EventHandler, CommandHandler, InteractionHandler         │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Redis 활용 방안

| 용도 | Key 패턴 | TTL | 설명 |
|------|----------|-----|------|
| 토큰 캐싱 | `slack:token:{teamId}` | 55분 | DB 조회 감소 |
| 이벤트 중복 | `slack:event:{eventId}` | 1시간 | 멱등성 보장 |
| Rate Limiting | `slack:rate:{teamId}:{minute}` | 2분 | 분당 요청 제한 |
| OAuth State | `slack:oauth:{state}` | 10분 | CSRF 방지 |

---

## 4. 패키지 구조

```
dop-global-apps-server/src/main/java/.../server/
├── config/
│   ├── RedisConfig.java
│   └── CryptoConfig.java
├── slack/
│   ├── adapter/SlackBoltAdapter.java
│   ├── controller/SlackController.java
│   ├── service/
│   │   ├── SlackTokenService.java
│   │   ├── SlackOAuthService.java
│   │   └── SlackWorkspaceService.java
│   ├── repository/
│   │   ├── SlackWorkspaceRepository.java
│   │   └── SlackEventLogRepository.java
│   ├── entity/
│   │   ├── SlackWorkspace.java
│   │   ├── SlackEventLog.java
│   │   └── WorkspaceStatus.java
│   └── redis/
│       ├── SlackEventDeduplicator.java
│       ├── SlackRateLimiter.java
│       └── SlackOAuthStateStore.java

dop-global-apps-core/src/main/java/.../core/
├── crypto/                              # 암호화 공통 모듈
│   ├── EncryptionService.java           # 인터페이스
│   ├── Aes256EncryptionService.java     # AES-256 구현체
│   ├── EncryptionProperties.java        # 설정 클래스
│   └── EncryptedStringConverter.java    # JPA AttributeConverter
├── slack/
│   ├── SlackBoltExtension.java
│   ├── SlackTokenProvider.java
│   └── dto/SlackInstallation.java

plugins/slack-plugin/src/main/java/.../plugin/slack/
├── SlackBoltExtensionImpl.java
└── handler/
    ├── EventHandler.java
    ├── CommandHandler.java
    └── InteractionHandler.java
```

---

## 4.1 암호화 공통 모듈 (Core) - Jasypt 기반

> dop-chat 프로젝트와 동일한 방식 채택

### 의존성 추가
```toml
# libs.versions.toml
jasypt = "3.0.5"

[libraries]
jasypt-spring-boot-starter = { module = "com.github.ulisesbocchio:jasypt-spring-boot-starter", version.ref = "jasypt" }
```

### JasyptConfig 설정 클래스
```java
@Configuration
@EnableEncryptableProperties
public class JasyptConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Bean
    public StringEncryptor jasyptStringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // Local: "local" / 운영: OPS_CONK 환경변수
        String encryptKey = "local".equals(activeProfile)
            ? "local"
            : System.getenv("OPS_CONK");

        config.setPassword(encryptKey);
        config.setPoolSize("1");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setStringOutputType("base64");
        config.setKeyObtentionIterations("1000");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        encryptor.setConfig(config);
        return encryptor;
    }
}
```

### EncryptedStringConverter (JPA 필드 암호화)
```java
@Converter
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final StringEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? encryptor.encrypt(attribute) : null;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData != null ? encryptor.decrypt(dbData) : null;
    }
}
```

### 사용 예시

**1. 설정 파일 암호화 (ENC 형식)**
```yaml
# application-prod.yml
slack:
  app:
    client-secret: ENC(암호화된_텍스트)
    signing-secret: ENC(암호화된_텍스트)

spring:
  data:
    redis:
      password: ENC(암호화된_텍스트)
```

**2. Entity 필드 암호화**
```java
@Entity
public class SlackWorkspace {
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "access_token")
    private String accessToken;  // DB 저장 시 자동 암호화
}
```

### 암호화 키 관리
| 환경 | 키 소스 |
|------|---------|
| Local | "local" (하드코딩) |
| Dev/Stg/Prod | `OPS_CONK` 환경변수 (K8s Secret) |

---

## 5. 핵심 구현 파일

### 5.1 SlackBoltExtension (Core)
```java
public interface SlackBoltExtension extends ExtensionPoint {
    void configureHandlers(App app);
    default int getOrder() { return 100; }
}
```

### 5.2 SlackBoltAdapter (Server)
- Bolt App 생성 및 관리
- 미들웨어 등록 (중복체크, Rate Limiting)
- 플러그인 핸들러 자동 등록

### 5.3 SlackController (Server)
- `POST /slack/events` - 이벤트 수신
- `POST /slack/commands` - 슬래시 커맨드
- `POST /slack/interactions` - 인터랙션
- `GET /slack/install` - OAuth 시작
- `GET /slack/oauth/callback` - OAuth 콜백

### 5.4 SlackWorkspace Entity
```java
@Entity
public class SlackWorkspace {
    private Long id;
    private String teamId;
    private String teamName;
    private String accessToken;
    private String botUserId;
    private WorkspaceStatus status;
    private Instant installedAt;
}
```

---

## 6. 설정 파일

### application.yml 추가
```yaml
slack:
  app:
    client-id: ${SLACK_CLIENT_ID}
    client-secret: ${SLACK_CLIENT_SECRET}
    signing-secret: ${SLACK_SIGNING_SECRET}
    scopes: channels:history,chat:write,commands,app_mentions:read

spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

---

## 7. 구현 순서

### Phase 1: 기반 구조
- [ ] libs.versions.toml 의존성 추가 (Slack Bolt, Jasypt, Redis)
- [ ] 각 모듈 build.gradle 수정
- [ ] JasyptConfig 작성 (Core 모듈)
- [ ] EncryptedStringConverter 작성 (Core 모듈)
- [ ] RedisConfig 작성
- [ ] Core 모듈 SlackBoltExtension 인터페이스 정의

### Phase 2: 서버 인프라
- [ ] SlackWorkspace Entity/Repository
- [ ] Redis 유틸리티 (Deduplicator, RateLimiter, StateStore)
- [ ] SlackTokenService (캐싱 포함)
- [ ] SlackWorkspaceService

### Phase 3: Bolt 통합
- [ ] SlackBoltAdapter
- [ ] SlackController
- [ ] SlackOAuthService

### Phase 4: 플러그인 구현
- [ ] SlackBoltExtensionImpl
- [ ] 이벤트/커맨드/인터랙션 핸들러

### Phase 5: 테스트
- [ ] 단위 테스트
- [ ] 통합 테스트
- [ ] 로컬 E2E 테스트

---

## 8. 검증 방법

1. **빌드 확인**: `./gradlew clean build`
2. **로컬 실행**: `./gradlew bootRun` + Docker Redis
3. **Slack 테스트 앱 연동**: ngrok으로 로컬 노출 후 OAuth 플로우 테스트
4. **이벤트 테스트**: 앱 멘션, 슬래시 커맨드 실행
