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
├── dop-global-apps-core/           # 내부 유틸리티 (추후 구조 변경 예정)
├── dop-global-apps-domain/         # Entity, Enum, Repository 인터페이스
├── dop-global-apps-infrastructure/ # 기술 구현체 (JPA, Redis, Kafka, Crypto)
├── dop-global-apps-api/            # Controller, Service (Entry Point)
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
com.daou.dop.global.apps.plugin.sdk/
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

### 2.2 domain (도메인 모델)

```
com.daou.dop.global.apps.domain/
├── plugin/
│   ├── Plugin.java                  # Entity
│   └── PluginRepository.java        # Repository 인터페이스
│
├── company/
│   ├── Company.java
│   └── CompanyRepository.java
│
├── user/
│   ├── User.java
│   └── UserRepository.java
│
├── connection/
│   ├── PluginConnection.java
│   └── PluginConnectionRepository.java
│
├── credential/
│   ├── OAuthCredential.java
│   ├── OAuthCredentialRepository.java
│   ├── ApiKeyCredential.java
│   └── ApiKeyCredentialRepository.java
│
├── enums/
│   ├── AuthType.java
│   ├── ScopeType.java
│   ├── ConnectionStatus.java
│   ├── PluginStatus.java
│   ├── CompanyStatus.java
│   └── UserStatus.java
│
└── cache/
    └── TokenCacheRepository.java    # 캐시 인터페이스
```

**원칙**:
- Entity, Enum, Repository 인터페이스
- JPA 어노테이션 사용 (`jakarta.persistence-api`)
- 구현체 없음 (인터페이스만)

**의존성**:
```groovy
dependencies {
    compileOnly 'jakarta.persistence:jakarta.persistence-api'
}
```

### 2.3 infrastructure (기술 구현체)

```
com.daou.dop.global.apps.infrastructure/
├── persistence/                     # DB (JPA)
│   ├── JpaPluginRepository.java
│   ├── JpaCompanyRepository.java
│   ├── JpaUserRepository.java
│   ├── JpaPluginConnectionRepository.java
│   ├── JpaOAuthCredentialRepository.java
│   └── JpaApiKeyCredentialRepository.java
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
└── config/                          # 기타 설정
    ├── JpaConfig.java
    └── AsyncConfig.java
```

**원칙**:
- domain의 Repository 인터페이스 구현
- 모든 기술 구현체 (JPA, Redis, Kafka 등)
- 설정 클래스

**의존성**:
```groovy
dependencies {
    implementation project(':dop-global-apps-domain')
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter'
}
```

### 2.4 api (애플리케이션)

```
com.daou.dop.global.apps.api/
├── controller/
│   ├── PluginController.java        # 플러그인 목록/상세
│   ├── ConnectionController.java    # 연동 관리
│   ├── OAuthController.java         # OAuth 콜백
│   └── ExecuteController.java       # API 실행
│
├── service/
│   ├── PluginService.java           # 플러그인 조회
│   ├── ConnectionService.java       # 연동 생성/조회/삭제
│   ├── OAuthService.java            # OAuth 플로우
│   ├── CredentialService.java       # 토큰/API Key 관리
│   └── ExecuteService.java          # 플러그인 API 실행
│
├── dto/
│   ├── request/
│   │   ├── ExecuteRequest.java
│   │   └── ConnectionRequest.java
│   └── response/
│       ├── PluginResponse.java
│       └── ConnectionResponse.java
│
├── plugin/
│   └── PluginRegistry.java          # 플러그인 확장점 관리
│
└── DopGlobalAppsApplication.java    # Entry Point
```

**원칙**:
- Controller + Service
- API용 Request/Response DTO
- Spring Boot Entry Point

**의존성**:
```groovy
dependencies {
    implementation project(':plugins:plugin-sdk')
    implementation project(':dop-global-apps-domain')
    runtimeOnly project(':dop-global-apps-infrastructure')
    runtimeOnly project(':plugins:slack-plugin')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.pf4j:pf4j-spring:0.9.0'
}
```

### 2.5 plugins/slack-plugin (플러그인 구현체)

```
com.daou.dop.global.apps.plugin.slack/
├── SlackPlugin.java                 # PF4J Plugin 진입점
├── SlackOAuthHandler.java           # @Extension - OAuth 처리
├── SlackPluginExecutor.java         # @Extension - API 실행
└── handler/
    ├── EventHandler.java            # 이벤트 처리 (추후)
    └── CommandHandler.java          # 슬래시 커맨드 (추후)
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
┌─────────────────────────────────────────────────────────────┐
│                         api 모듈                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Controller                                           │   │
│  │  PluginCtrl, ConnectionCtrl, OAuthCtrl, ExecuteCtrl  │   │
│  └────────────────────────┬────────────────────────────┘   │
│                           │                                 │
│  ┌────────────────────────▼────────────────────────────┐   │
│  │ Service                                              │   │
│  │  PluginSvc, ConnectionSvc, OAuthSvc, ExecuteSvc      │   │
│  └────────────────────────┬────────────────────────────┘   │
└───────────────────────────┼─────────────────────────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────┐  ┌─────────────┐  ┌─────────────────┐
│  plugin-sdk     │  │ domain 모듈 │  │infrastructure 모듈│
│                 │  │             │  │                 │
│ PluginExecutor  │  │ Entity      │  │ JpaRepository   │
│ OAuthHandler    │  │ Enum        │  │ RedisCache      │
│ DTO             │  │ Repository  │  │ Kafka           │
│                 │  │ (interface) │  │ Crypto          │
└────────┬────────┘  └──────▲──────┘  └────────┬────────┘
         │                  │                  │
         │                  └──────────────────┘
         │                        구현
         ▼
┌─────────────────┐
│  slack-plugin   │
│  google-plugin  │
│  외부 플러그인   │
└─────────────────┘
```

---

## 4. 모듈 의존성

### 4.1 의존성 방향

```
                 plugin-sdk
                     ↑
        ┌────────────┼────────────┐
        │            │            │
       api ──────▶ domain ◀──── infrastructure
        │                         │
        └─────── runtimeOnly ─────┘
        │
        └─────── runtimeOnly ─────▶ slack-plugin
                                         │
                                         │ compileOnly
                                         ▼
                                    plugin-sdk
```

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

// infrastructure/build.gradle
dependencies {
    implementation project(':dop-global-apps-domain')
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter'
}

// api/build.gradle
dependencies {
    implementation project(':plugins:plugin-sdk')
    implementation project(':dop-global-apps-domain')
    runtimeOnly project(':dop-global-apps-infrastructure')
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
│                            domain 모듈                                 │
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ OAuthCredentialRepository (interface)                             ││
│  │   Optional<OAuthCredential> findByConnectionId(Long id);          ││
│  └──────────────────────────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ OAuthCredential (Entity)                                          ││
│  │   @Convert(converter = EncryptedStringConverter.class)            ││
│  │   private String accessToken;                                     ││
│  └──────────────────────────────────────────────────────────────────┘│
└───────────────────────────────────▲───────────────────────────────────┘
                                    │ 구현
┌───────────────────────────────────┴───────────────────────────────────┐
│                        infrastructure 모듈                             │
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ JpaOAuthCredentialRepository                                      ││
│  │   extends JpaRepository, OAuthCredentialRepository                ││
│  └──────────────────────────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────────────────────────┐│
│  │ EncryptedStringConverter                                          ││
│  │   - DB 조회 시 자동 복호화                                         ││
│  └──────────────────────────────────────────────────────────────────┘│
└───────────────────────────────────────────────────────────────────────┘
```

### 6.2 OAuth 설치 플로우

```
Client                api 모듈                     domain        infrastructure       Plugin
  │                      │                           │                │                │
  │ GET /oauth/slack/install                         │                │                │
  │─────────────────────▶│                           │                │                │
  │                      │                           │                │                │
  │                      │ pluginRepo.findByPluginId("slack")         │                │
  │                      │──────────────────────────▶│                │                │
  │                      │                           │◀───────────────│                │
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
| Redis | `cache/` | 토큰 캐시, OAuth State 저장 |
| Kafka | `messaging/` | 이벤트 발행/구독 |
| Jasypt | `crypto/` | 민감 정보 암호화 |
| RestClient | `external/` | 외부 API 호출 |

### 7.2 인터페이스-구현 분리 예시

```java
// domain 모듈 - 인터페이스
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

- domain에 인터페이스 정의
- infrastructure에서 구현
- api는 인터페이스에만 의존

### 8.2 모듈 경계

- 각 모듈은 명확한 책임
- 순환 의존성 금지
- runtimeOnly로 느슨한 결합

### 8.3 기술 교체 용이

- Redis → Memcached: infrastructure만 수정
- JPA → MyBatis: infrastructure만 수정
- 비즈니스 로직 영향 없음

---

## 9. 변경 이력

| 날짜 | 버전 | 내용 |
|------|------|------|
| 2025-01-21 | 0.1 | 초안 작성 |
| 2025-01-21 | 0.2 | 4 모듈 구조로 변경, infrastructure 상세화 |
| 2025-01-21 | 0.3 | plugin-sdk 모듈 추가, core → plugin-sdk로 의존성 변경 |
