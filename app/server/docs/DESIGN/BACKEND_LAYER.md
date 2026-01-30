# 백엔드 레이어 설계

## 개요

플러그인 시스템의 백엔드 모듈 및 레이어 구조 설계

---

## 1. 모듈 구조

### 1.1 현재 문제점

- core 모듈이 너무 많은 책임 (DB 접근, 암호화, 인터페이스 혼재)
- OAuth 로직이 core/server/plugin에 분산
- 레이어 분리 미흡

### 1.2 새로운 모듈 구조

```
server/
├── dop-gapps-core/           # Repository Port 인터페이스, StateStorage
├── dop-gapps-domain/         # Entity, Enum
├── dop-gapps-infrastructure/ # 기술 구현체 (JPA, Redis, Kafka, Crypto)
├── dop-gapps-api/            # Controller, Service (Entry Point)
│
└── plugins/
    ├── plugin-sdk/                 # 플러그인 공통 SDK (외부 배포)
    ├── slack-plugin/               # Slack 플러그인 구현
    └── google-plugin/              # Google 플러그인 구현 (예정)
```

---

## 2. 모듈별 책임

### 2.1 plugins/plugin-sdk (플러그인 SDK)

플러그인 개발에 필요한 인터페이스와 DTO 제공. 외부 개발자가 JAR만 의존하여 플러그인 개발 가능.

```
com.daou.dop.gapps.plugin.sdk/
├── PluginExecutor.java              # API 실행 인터페이스
├── OAuthHandler.java                # OAuth 처리 인터페이스
├── OAuthException.java              # OAuth 예외
│
├── PluginConfig.java                # 서버→플러그인 (설정)
├── CredentialContext.java           # 서버→플러그인 (인증정보)
├── ExecuteRequest.java              # API 실행 요청
├── ExecuteResponse.java             # API 실행 응답
└── TokenInfo.java                   # 플러그인→서버 (토큰)
```

**원칙**:
- 플러그인이 구현할 인터페이스만 포함
- 서버 ↔ 플러그인 간 데이터 교환 DTO
- Spring 의존성 없음 (PF4J만 의존)
- Maven Central / Nexus 배포 가능

**의존성**:
```groovy
// plugins/plugin-sdk/build.gradle
plugins {
    id 'java-library'
    id 'maven-publish'
}

dependencies {
    compileOnly 'org.pf4j:pf4j:3.14.1'
}
```

> 상세 내용: [PLUGIN.md](PLUGIN.md) 참조

### 2.2 core (핵심 비즈니스 로직)

```
com.daou.dop.gapps.core/
├── repository/                      # Repository Port 인터페이스
│   ├── PluginRepository.java
│   ├── PluginConnectionRepository.java
│   └── OAuthCredentialRepository.java
│
├── dto/                             # api에서 사용하는 DTO
│   ├── ExecuteCommand.java          # API 실행 요청
│   ├── ExecuteResult.java           # API 실행 응답
│   ├── CredentialInfo.java          # 인증 정보
│   ├── OAuthTokenInfo.java          # OAuth 토큰 정보
│   ├── PluginConfigInfo.java        # 플러그인 설정
│   └── ConnectionInfo.java          # 연동 정보
│
├── enums/
│   └── ScopeType.java               # 연동 범위 (WORKSPACE, USER)
│
├── oauth/
│   ├── StateStorage.java            # OAuth State 저장소 인터페이스
│   ├── PluginOAuthService.java      # OAuth 서비스 인터페이스
│   └── OAuthException.java          # OAuth 예외
│
├── credential/
│   └── CredentialProvider.java      # 인증 정보 조회 인터페이스
│
├── plugin/
│   ├── PluginRegistry.java          # 플러그인 확장점 관리
│   └── PluginService.java           # 플러그인 조회
│
├── connection/
│   └── ConnectionService.java       # 연동 관리 (CredentialProvider 구현)
│
└── execute/
    └── PluginExecutorService.java   # 플러그인 실행
```

**원칙**:
- Repository Port 인터페이스 (DIP)
- 비즈니스 로직 서비스
- api에서 사용할 DTO 정의 (plugin-sdk, domain 타입 숨김)
- plugin-sdk 타입 → core DTO 변환 담당

**의존성**:
```groovy
dependencies {
    implementation project(':dop-gapps-domain')  // 전이 불가
    implementation project(':plugins:plugin-sdk')      // 전이 불가

    api(libs.pf4j)
    compileOnly 'jakarta.persistence:jakarta.persistence-api'
    compileOnly 'org.springframework:spring-context'
    compileOnly 'org.springframework:spring-tx'
    compileOnly 'tools.jackson.core:jackson-databind'
}
```

> **Note**: `implementation`으로 선언하여 api 모듈에서 domain/plugin-sdk 타입에 직접 접근 불가

### 2.3 domain (도메인 모델)

```
com.daou.dop.gapps.domain/
├── plugin/
│   └── Plugin.java                  # Entity
│
├── company/
│   └── Company.java
│
├── user/
│   └── User.java
│
├── connection/
│   └── PluginConnection.java
│
├── credential/
│   ├── OAuthCredential.java
│   └── ApiKeyCredential.java
│
└── enums/
    ├── AuthType.java
    ├── ScopeType.java
    ├── ConnectionStatus.java
    ├── PluginStatus.java
    ├── CompanyStatus.java
    └── UserStatus.java
```

**원칙**:
- Entity, Enum만 포함
- JPA 어노테이션 사용 (`jakarta.persistence-api`)
- 순수 도메인 객체

**의존성**:
```groovy
dependencies {
    compileOnly 'jakarta.persistence:jakarta.persistence-api'
}
```

### 2.4 infrastructure (기술 구현체)

```
com.daou.dop.gapps.infrastructure/
├── persistence/                     # DB (JPA) - core Repository 구현
│   ├── JpaPluginRepository.java
│   ├── JpaCompanyRepository.java
│   ├── JpaUserRepository.java
│   ├── JpaPluginConnectionRepository.java
│   ├── JpaOAuthCredentialRepository.java
│   └── JpaApiKeyCredentialRepository.java
│
├── oauth/                           # OAuth 관련 구현
│   └── InMemoryStateStorage.java    # StateStorage 구현
│
├── cache/                           # 캐시 (Redis)
│   ├── RedisCacheConfig.java
│   └── RedisTokenCacheRepository.java
│
├── messaging/                       # 메시징 (Kafka)
│   ├── KafkaConfig.java
│   ├── KafkaEventPublisher.java
│   └── KafkaEventConsumer.java
│
├── crypto/                          # 암호화
│   ├── JasyptConfig.java
│   ├── EncryptedStringConverter.java
│   └── EncryptedJsonConverter.java
│
├── external/                        # 외부 API
│   └── RestClientConfig.java
│
├── config/                          # 기타 설정
│   ├── JpaConfig.java
│   └── AsyncConfig.java
│
└── resources/
    └── db/migration/                # Flyway 마이그레이션 파일
        ├── develop/                 # 개발 환경
        │   └── V1__create_tables.sql
        └── release/                 # 운영 환경
            └── V1__create_tables.sql
```

**원칙**:
- core의 Repository Port 인터페이스 구현
- 모든 기술 구현체 (JPA, Redis, Kafka 등)
- 설정 클래스

**의존성**:
```groovy
dependencies {
    implementation project(':dop-gapps-core')
    implementation project(':dop-gapps-domain')
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter'

    // Flyway (DB 마이그레이션)
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'
}
```

### 2.5 api (HTTP 진입점)

```
com.daou.dop.gapps.api/
├── oauth/
│   └── controller/
│       └── PluginOAuthController.java  # OAuth 설치/콜백
│
├── plugin/
│   ├── controller/
│   │   └── PluginController.java       # 플러그인 API
│   └── service/
│       └── PluginResourceService.java  # 플러그인 리소스 조회
│
├── execute/
│   └── ExecuteController.java          # API 실행 엔드포인트
│
├── config/
│   └── ...                             # 설정 클래스
│
└── DopGlobalAppsApiApplication.java    # Entry Point
```

**원칙**:
- HTTP Controller만 포함 (진입점 역할)
- 비즈니스 로직은 core 서비스에 위임
- **core DTO만 사용** (domain Entity, plugin-sdk 타입 직접 사용 불가)

**의존성**:
```groovy
dependencies {
    // Core (DTO, Service, Repository Port)
    implementation project(':dop-gapps-core')

    // Infrastructure (Repository 구현체, JpaConfig - 런타임 주입용)
    implementation project(':dop-gapps-infrastructure')

    // 플러그인 (런타임 로딩)
    runtimeOnly project(':plugins:slack-plugin')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.pf4j:pf4j-spring:0.9.0'
}
```

> **중요**: api 모듈은 domain, plugin-sdk를 직접 의존하지 않음. core가 `implementation`으로 의존하므로 타입 전이 불가.

### 2.6 plugins/slack-plugin (플러그인 구현체)

```
com.daou.dop.gapps.plugin.slack/
├── SlackPlugin.java                 # PF4J Plugin 진입점
├── SlackOAuthHandler.java           # @Extension - OAuth 처리
└── SlackPluginExecutor.java         # @Extension - API 실행
```

**의존성**:
```groovy
dependencies {
    compileOnly project(':plugins:plugin-sdk')
    compileOnly 'org.pf4j:pf4j:3.14.1'
    implementation 'com.slack.api:bolt:1.44.2'
}
```

---

## 3. 레이어 다이어그램

```
┌─────────────────────────────────────────────────────────────────┐
│                           api 모듈                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Controller (HTTP 진입점)                                  │   │
│  │  OAuthCtrl, ExecuteCtrl, PluginCtrl                       │   │
│  └────────────────────────┬─────────────────────────────────┘   │
│                           │ core DTO만 사용                      │
│                           │ (domain, plugin-sdk 타입 접근 불가)  │
└───────────────────────────┼─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                          core 모듈                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ DTO: ExecuteCommand, ExecuteResult, CredentialInfo, ...  │   │
│  │ Enum: ScopeType                                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Service: PluginSvc, ConnectionSvc, PluginExecutorSvc     │   │
│  │ Interface: PluginOAuthService, CredentialProvider        │   │
│  └────────────────┬─────────────────────┬───────────────────┘   │
│                   │                     │                       │
│                   ▼                     ▼                       │
│         ┌─────────────────┐   ┌─────────────────┐               │
│         │ domain 타입     │   │ plugin-sdk 타입 │               │
│         │ (implementation)│   │ (implementation)│               │
│         │ 내부 변환       │   │ 내부 변환       │               │
│         └────────┬────────┘   └────────┬────────┘               │
│  ┌───────────────┴─────────────────────┴────────────────────┐   │
│  │ Repository Port 인터페이스                                │   │
│  │  PluginRepo, ConnectionRepo, CredentialRepo              │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            │
          ┌─────────────────┴─────────────────┐
          │ implements                        │
          ▼                                   ▼
┌─────────────────┐                 ┌───────────────────┐
│  plugin-sdk     │                 │infrastructure 모듈│
│                 │                 │                   │
│ PluginExecutor  │                 │ JpaRepository     │
│ OAuthHandler    │                 │ StateStorage      │
│ SDK DTO         │                 │ Crypto            │
└────────┬────────┘                 └───────────────────┘
         │
         │ compileOnly
         ▼
┌─────────────────┐
│  slack-plugin   │
│  google-plugin  │
│  외부 플러그인  │
└─────────────────┘
```

---

## 4. 모듈 의존성

### 4.1 의존성 방향 (Clean Architecture + 타입 격리)

```
       ┌─────────────────────────────────────────────────────────┐
       │                          api                            │
       └───────────────┬─────────────────────┬───────────────────┘
                       │                     │
                       │ impl                │ runtimeOnly (classpath용)
                       ▼                     ▼
       ┌───────────────────────┐      ┌─────────────────────────────┐
       │         core          │◀─────│      infrastructure         │
       │   (Repository Port)   │ impl │   (Repository 구현체)       │
       └───────┬───────┬───────┘      └───────────┬─────────────────┘
               │       │                          │
               │       │ impl (전이 불가)         │ impl
               │       ▼                          │
               │  ┌─────────────┐                 │
               │  │ plugin-sdk  │                 │
               │  └─────────────┘                 │
               │                                  │
               │ impl (전이 불가)                 │
               ▼                                  ▼
       ┌───────────────────────────────────────────────────────┐
       │                        domain                         │
       └───────────────────────────────────────────────────────┘

       infrastructure ─── impl ───▶ core (인터페이스 구현)
       slack-plugin ─── compileOnly ───▶ plugin-sdk
       api ─── runtimeOnly ───▶ slack-plugin
```

**핵심 원칙**:
- core가 domain, plugin-sdk를 `implementation`으로 의존 → api에서 타입 전이 불가
- api는 **core DTO만 사용** (Entity, plugin-sdk 타입 직접 접근 불가)
- api → infrastructure는 `runtimeOnly` (타입 의존 없음, classpath 포함만)
- 모든 타입 변환은 core에서 수행

**장점**:
- api 레이어가 도메인 엔티티에 직접 의존하지 않음
- api 레이어가 infrastructure 타입에도 의존하지 않음
- 플러그인 SDK 변경이 api에 영향 없음
- 계층 간 명확한 경계

### 4.2 Gradle 설정

```groovy
// plugins/plugin-sdk/build.gradle
plugins {
    id 'java-library'
    id 'maven-publish'
}

dependencies {
    compileOnly 'org.pf4j:pf4j:3.14.1'
}

// domain/build.gradle
dependencies {
    compileOnly 'jakarta.persistence:jakarta.persistence-api'
}

// core/build.gradle (타입 격리 핵심)
dependencies {
    // implementation = 전이 불가 → api에서 domain, plugin-sdk 타입 직접 사용 불가
    implementation project(':dop-gapps-domain')
    implementation project(':plugins:plugin-sdk')

    api(libs.pf4j)
    api(libs.jasypt.spring.boot.starter)
    compileOnly 'jakarta.persistence:jakarta.persistence-api'
    compileOnly 'org.springframework:spring-context'
    compileOnly 'org.springframework:spring-tx'
    compileOnly 'tools.jackson.core:jackson-databind'
}

// infrastructure/build.gradle
dependencies {
    implementation project(':dop-gapps-core')
    implementation project(':dop-gapps-domain')
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.kafka:spring-kafka'
}

// api/build.gradle (domain, plugin-sdk, infrastructure 타입 직접 사용 없음)
dependencies {
    implementation project(':dop-gapps-core')
    runtimeOnly project(':dop-gapps-infrastructure')  // classpath용 (타입 의존 없음)

    // plugin-sdk는 직접 의존하지 않음 - core를 통해 사용
    runtimeOnly project(':plugins:slack-plugin')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.pf4j:pf4j-spring:0.9.0'
}

// plugins/slack-plugin/build.gradle
dependencies {
    compileOnly project(':plugins:plugin-sdk')
    compileOnly 'org.pf4j:pf4j:3.14.1'
    implementation 'com.slack.api:bolt:1.44.2'
}
```

---

## 5. API 엔드포인트

### 5.1 플러그인 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/plugins` | 플러그인 목록 조회 |
| GET | `/api/plugins/{pluginId}` | 플러그인 상세 조회 |

### 5.2 연동 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/connections` | 연동 목록 조회 |
| GET | `/api/connections/{id}` | 연동 상세 조회 |
| DELETE | `/api/connections/{id}` | 연동 해제 |

### 5.3 OAuth

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/oauth/{pluginId}/install` | OAuth 설치 시작 |
| GET | `/oauth/{pluginId}/callback` | OAuth 콜백 처리 |

### 5.4 API 실행

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/execute` | 플러그인 API 실행 |

---

## 6. 데이터 흐름

### 6.1 OAuth 토큰 조회 플로우

```
┌───────────────────────────────────────────────────────────────────────┐
│                              api 모듈                                  │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ ExecuteController                                                │ │
│  │   POST /api/execute                                              │ │
│  └──────────────────────────────┬──────────────────────────────────┘ │
│                                 │                                    │
│  ┌──────────────────────────────▼──────────────────────────────────┐ │
│  │ ExecuteService                                                   │ │
│  │   1. connectionRepo.findById()                                   │ │
│  │   2. credentialRepo.findByConnectionId()                         │ │
│  │   3. pluginExecutor.execute()                                    │ │
│  └──────────────────────────────┬──────────────────────────────────┘ │
└─────────────────────────────────┼────────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────────┐
│                            core 모듈                                   │
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ OAuthCredentialRepository (interface) - Repository Port           ││
│  │   Optional<OAuthCredential> findByConnectionId(Long id);          ││
│  └──────────────────────────────────────────────────────────────────┘│
└───────────────────────────────────┬───────────────────────────────────┘
                                    │ 의존
┌───────────────────────────────────▼───────────────────────────────────┐
│                            domain 모듈                                 │
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ OAuthCredential (Entity)                                          ││
│  │   @Convert(converter = EncryptedStringConverter.class)            ││
│  │   private String accessToken;                                     ││
│  └──────────────────────────────────────────────────────────────────┘│
└───────────────────────────────────▲───────────────────────────────────┘
                                    │ 구현 (core Port)
┌───────────────────────────────────┴───────────────────────────────────┐
│                        infrastructure 모듈                             │
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ JpaOAuthCredentialRepository                                      ││
│  │   extends JpaRepository                                           ││
│  │   implements OAuthCredentialRepository (core Port)                ││
│  └──────────────────────────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ EncryptedStringConverter                                          ││
│  │   - DB 조회 시 자동 복호화                                         ││
│  └──────────────────────────────────────────────────────────────────┘│
└───────────────────────────────────────────────────────────────────────┘
```

### 6.2 OAuth 설치 플로우

```
Client                api 모듈                       core        infrastructure       Plugin
  │                      │                           │                │                │
  │ GET /oauth/slack/install                         │                │                │
  │─────────────────────▶│                           │                │                │
  │                      │                           │                │                │
  │                      │ pluginRepo.findByPluginId("slack")         │                │
  │                      │──────────────────────────▶│ (Port)         │                │
  │                      │                           │◀───────────────│ (구현체)       │
  │                      │◀──────────────────────────│                │                │
  │                      │                           │                │                │
  │                      │ authHandler.buildAuthUrl()│                │                │
  │                      │────────────────────────────────────────────────────────────▶│
  │                      │◀────────────────────────────────────────────────────────────│
  │                      │                           │                │                │
  │◀─ 302 Redirect ──────│                           │                │                │
  │                      │                           │                │                │
```

---

## 7. infrastructure 상세

### 7.1 기술별 분류

| 기술 | 패키지 | 용도 |
|------|--------|------|
| JPA | `persistence/` | DB 접근 |
| Flyway | `db/migration/` | DB 스키마 마이그레이션 |
| Redis | `cache/` | 토큰 캐시, OAuth State 저장 |
| Kafka | `messaging/` | 이벤트 발행/구독 |
| Jasypt | `crypto/` | 민감 정보 암호화 |
| RestClient | `external/` | 외부 API 호출 |

### 7.2 인터페이스-구현 분리 예시

```java
// core 모듈 - Repository Port 인터페이스
public interface TokenCacheRepository {
    Optional<String> get(String key);
    void set(String key, String value, Duration ttl);
    void delete(String key);
}

// infrastructure 모듈 - Redis 구현
@Repository
@RequiredArgsConstructor
public class RedisTokenCacheRepository implements TokenCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
```

---

## 8. 설계 원칙

### 8.1 DIP (의존성 역전)

- core에 Repository Port 인터페이스 정의
- infrastructure에서 구현
- api는 core 인터페이스에 의존 (infrastructure는 JpaConfig 로딩을 위해 implementation)

### 8.2 모듈 경계

- 각 모듈은 명확한 책임
- 순환 의존성 금지
- runtimeOnly로 느슨한 결합

### 8.3 기술 교체 용이

- Redis → Memcached: infrastructure만 수정
- JPA → MyBatis: infrastructure만 수정
- 비즈니스 로직 영향 없음

---

## 9. DB 마이그레이션 (Flyway)

### 9.1 개요

- 마이그레이션 파일: `infrastructure/src/main/resources/db/migration/`
- 실행 시점: 앱 시작 시 자동 실행 (local/dev), CI/CD에서 별도 실행 (prod)
- 공유 스키마이므로 infrastructure 모듈에서 관리

### 9.2 환경별 설정

| 환경 | flyway.enabled | 마이그레이션 위치 | 설명 |
|------|---------------|-----------------|------|
| local | `true` | `classpath:db/migration/develop` | 앱 시작 시 자동 실행 |
| dev | `true` | `classpath:db/migration/develop` | 앱 시작 시 자동 실행 |
| prod | `false` | - | CI/CD에서 Gradle 태스크로 실행 |

### 9.3 설정 예시

```yaml
# application-local.yml (또는 application-dev.yml)
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration/develop

  jpa:
    hibernate:
      ddl-auto: validate  # Flyway가 스키마 관리
```

```yaml
# application-prod.yml
spring:
  flyway:
    enabled: false  # CI/CD에서 별도 실행
```

### 9.4 운영 환경 마이그레이션 (Gradle 태스크)

```bash
# CI/CD에서 앱 배포 전 실행
FLYWAY_URL=jdbc:postgresql://prod-db:5432/global_apps \
FLYWAY_USER=admin \
FLYWAY_PASSWORD=secret \
FLYWAY_ENV=release \
./gradlew :dop-gapps-infrastructure:flywayMigrate
```

### 9.5 마이그레이션 파일 명명 규칙

```
V{버전}__{설명}.sql

예시:
V1__create_tables.sql
V2__add_column_to_plugin.sql
V3__create_index.sql
```

---

## 10. 새로운 Entry Point 추가 가이드

### 10.1 Entry Point란?

Spring Boot Application의 진입점. 예시:
- `api` - REST API 서버
- `batch` - 배치 작업
- `scheduler` - 스케줄러
- `consumer` - 메시지 컨슈머

### 10.2 왜 core와 infrastructure 둘 다 의존해야 하는가?

```
Entry Point → core (인터페이스 사용)
              ↑
      infrastructure (구현체) ← classpath에 있어야 Spring이 주입 가능
```

**핵심:**
- core는 **인터페이스만** 정의 (Repository Port)
- 실제 구현체는 infrastructure에 존재 (JpaRepository 등)
- Gradle 의존성 선언 없으면 **classpath에 포함 안 됨**
- Spring이 런타임에 빈을 주입하려면 구현체가 classpath에 있어야 함

**왜 core가 infrastructure를 의존하면 안 되는가?**
```
core → infrastructure  ← DIP 위반!
infrastructure → core  ← 올바른 방향 (구현이 인터페이스에 의존)
```
- 순환 의존성 발생
- core가 특정 기술(JPA)에 종속
- 기술 교체 시 core 수정 필요

### 10.3 새로운 Entry Point 추가 방법

**1. 모듈 생성**
```bash
mkdir -p dop-gapps-batch/src/main/java/com/daou/dop/global/apps/batch
```

**2. build.gradle 설정**
```groovy
// dop-gapps-batch/build.gradle
plugins {
    id 'org.springframework.boot'
}

dependencies {
    // 필수: core + infrastructure
    implementation project(':dop-gapps-core')
    implementation project(':dop-gapps-infrastructure')

    // 플러그인 사용 시
    implementation project(':plugins:plugin-sdk')
    runtimeOnly project(':plugins:slack-plugin')

    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
}
```

**3. Application 클래스**
```java
package com.daou.dop.gapps.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "com.daou.dop")
@EntityScan(basePackages = "com.daou.dop")
public class DopGlobalAppsBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(DopGlobalAppsBatchApplication.class, args);
    }
}
```

**4. settings.gradle 등록**
```groovy
include 'dop-gapps-batch'
```

### 10.4 체크리스트

| 항목 | 설명 |
|------|------|
| `implementation core` | Repository Port 인터페이스 사용 |
| `implementation infrastructure` | 런타임 구현체 주입 |
| `@EntityScan` | `com.daou.dop` - Entity 스캔 |
| `scanBasePackages` | `com.daou.dop` - 컴포넌트 스캔 |
| `flyway.enabled` | local/dev: `true`, prod: `false` (9장 참조) |

---

## 11. 변경 이력

| 날짜 | 버전 | 내용 |
|------|------|------|
| 2025-01-21 | 0.1 | 초안 작성 |
| 2025-01-21 | 0.2 | 4 모듈 구조로 변경, infrastructure 상세화 |
| 2025-01-21 | 0.3 | plugin-sdk 모듈 추가, core → plugin-sdk로 의존성 변경 |
| 2026-01-21 | 0.4 | Clean Architecture 적용: core에 Repository Port 정의, api→core→domain 의존성 구조 |
| 2026-01-21 | 0.5 | 새로운 Entry Point 추가 가이드 섹션 추가 |
| 2026-01-21 | 0.6 | Flyway DB 마이그레이션 섹션 추가 (infrastructure 모듈에서 관리) |
| 2026-01-22 | 0.7 | 타입 격리 적용: core가 domain/plugin-sdk를 implementation으로 의존, api는 core DTO만 사용 |
