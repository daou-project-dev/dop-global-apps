# DOP Global Apps

다우오피스 글로벌 앱 통합 프로젝트

## 프로젝트 구조

```
dop-global-apps/
├── app/
│   └── server/                     # 백엔드 서버
│       ├── dop-global-apps-api/        # Controller (HTTP 진입점)
│       ├── dop-global-apps-core/       # DTO, Service, Repository Port
│       ├── dop-global-apps-domain/     # Entity, Enum
│       ├── dop-global-apps-infrastructure/  # JPA, Crypto 구현체
│       └── plugins/
│           ├── plugin-sdk/             # 플러그인 공통 SDK
│           └── slack-plugin/           # Slack 플러그인
└── docs/                           # 공통 문서
```

## 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      API Layer (api)                        │
│              Controller (HTTP 진입점, core DTO 사용)          │
├─────────────────────────────────────────────────────────────┤
│                      Core Layer (core)                      │
│  DTO, Service, Repository Port, plugin-sdk/domain 타입 변환   │
├──────────────────────────┬──────────────────────────────────┤
│   Domain (Entity, Enum)  │   Infrastructure (JPA, Crypto)   │
│     (impl - 전이 불가)    │     → core (인터페이스 구현)       │
└──────────────────────────┴──────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Plugin System (PF4J)                     │
│        plugin-sdk (impl - 전이 불가)  ←──  slack-plugin      │
└─────────────────────────────────────────────────────────────┘
```

### 의존성 흐름

| 관계 | 타입 | 설명 |
|------|------|------|
| api → core | impl | 비즈니스 로직 사용 |
| api → infrastructure | runtimeOnly | classpath용 (타입 의존 없음) |
| infrastructure → core | impl | Repository 인터페이스 구현 (DIP) |
| core → domain | impl | 전이 불가 (타입 격리) |
| core → plugin-sdk | impl | 전이 불가 (타입 격리) |

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 25 + Virtual Threads |
| Framework | Spring Boot 4.0.1 |
| Plugin | PF4J 3.14.1 |
| Database | PostgreSQL / H2 |
| Migration | Flyway 11.x |

## 시작하기

```bash
cd app/server
./gradlew bootRun
```

상세 실행 방법: [app/server/README.md](app/server/README.md)

## 문서

- [서버 가이드](app/server/README.md)
- [설계 문서](app/server/docs/DESIGN/)
- [Entry Point 추가 가이드](app/server/docs/DESIGN/BACKEND_LAYER.md#9-새로운-entry-point-추가-가이드)

## 라이선스

Apache License 2.0 - [LICENSE](LICENSE)
