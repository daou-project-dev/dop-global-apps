# CLAUDE.md (Backend)

백엔드 서버 모듈 작업 시 참고 사항

## 기술 스택

- Java 25 + Virtual Threads
- Spring Boot 4.0.1
- Gradle 9.x (Groovy DSL)
- PF4J 3.14.1 (플러그인 시스템)
- PostgreSQL (개발/운영) / H2 (로컬)

## 모듈 구조

```
server/
├── dop-global-apps-core/    # 공통 유틸리티, 플러그인 인터페이스
├── dop-global-apps-server/  # Entry Point, Spring Boot 애플리케이션
└── plugins/
    └── slack-plugin/        # Slack 연동 플러그인
```

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
