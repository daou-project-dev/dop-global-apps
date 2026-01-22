# CLAUDE.md (Backend)

백엔드 서버 모듈 작업 시 참고 사항

## 기술 스택

- Java 25 + Virtual Threads
- Spring Boot 4.0.1
- Gradle 9.x (Groovy DSL)
- PF4J 3.14.1 (플러그인 시스템)
- PostgreSQL (개발/운영) / H2 (로컬)

## 설계 문서

- [백엔드 레이어 설계](docs/DESIGN/BACKEND_LAYER.md) - 모듈 구조, 의존성, API
- [플러그인 아키텍처](docs/DESIGN/PLUGIN.md) - SDK, 플러그인 개발
- [도메인 모델](docs/DESIGN/DOMAIN.md) - Entity, Enum

## 모듈 구조

```
server/
├── dop-global-apps-api/     # HTTP 진입점 (Controller)
├── dop-global-apps-core/    # 비즈니스 로직 (DTO, Service, Repository Port)
├── dop-global-apps-domain/  # 도메인 모델 (Entity, Enum)
├── dop-global-apps-infrastructure/  # 기술 구현체 (JPA, Crypto)
└── plugins/
    ├── plugin-sdk/          # 플러그인 SDK
    └── slack-plugin/        # Slack 연동 플러그인
```

> 의존성 구조는 루트 [CLAUDE.md](../../CLAUDE.md) 참조

## 빌드 명령어

```bash
# 빌드
./gradlew build

# 실행 (로컬 H2)
./gradlew bootRun

# 실행 (Dev PostgreSQL)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 클린 빌드
./gradlew clean build
```

## 코딩 규칙

- Lombok 사용 (`@Getter`, `@Builder` 등)
- 패키지 구조: `com.daou.dop.global.apps.*`
- 플러그인 클래스: `@Extension` 어노테이션 필수
- 테스트: JUnit 5 + Spring Boot Test

## 의존성 관리

- Version Catalog 사용: `gradle/libs.versions.toml`
- 모듈 간 의존성: `implementation project(':dop-global-apps-core')`
