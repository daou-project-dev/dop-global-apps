# DOP Global Apps

다우오피스 글로벌 앱 통합 프로젝트

## 프로젝트 구조

```
dop-global-apps/
├── app/
│   ├── client/                     # 프론트엔드 (React)
│   │   ├── src/
│   │   │   ├── api/                    # 공통 API 클라이언트
│   │   │   ├── components/             # 공통 UI 컴포넌트
│   │   │   ├── pages/                  # 페이지 컴포넌트
│   │   │   ├── store/                  # Jotai atoms
│   │   │   └── routes/                 # 라우터 설정
│   │   └── vite.config.ts
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

### Backend

| 분류 | 기술 |
|------|------|
| Language | Java 25 + Virtual Threads |
| Framework | Spring Boot 4.0.1 |
| Plugin | PF4J 3.14.1 |
| Database | PostgreSQL / H2 |
| Migration | Flyway 11.x |

### Frontend

| 분류 | 기술 |
|------|------|
| Framework | React 19 |
| Language | TypeScript 5.9 |
| Build Tool | Vite 7 |
| 데이터 페칭 | TanStack Query 5 |
| 상태관리 | Jotai 2 |
| 스타일링 | CSS Modules |
| 최적화 | React Compiler |

## 시작하기

### Backend

```bash
cd app/server
./gradlew bootRun
```

### Frontend

```bash
cd app/client
pnpm install
pnpm dev
```

상세 실행 방법: [서버](app/server/README.md) | [클라이언트](app/client/README.md)

## 문서

- [서버 가이드](app/server/README.md)
- [클라이언트 가이드](app/client/CLAUDE.md)
- [설계 문서](app/server/docs/DESIGN/)
- [Entry Point 추가 가이드](app/server/docs/DESIGN/BACKEND_LAYER.md#9-새로운-entry-point-추가-가이드)

## 라이선스

Apache License 2.0 - [LICENSE](LICENSE)
