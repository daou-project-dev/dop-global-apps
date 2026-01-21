# DOP Global Apps

다우오피스 글로벌 앱 통합 프로젝트

## 프로젝트 구조

```
dop-global-apps/
├── app/
│   └── server/                 # 백엔드 서버
└── docs/                       # 공통 문서
```

## 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      API Layer (api)                        │
├─────────────────────────────────────────────────────────────┤
│                    Domain Layer (domain)                    │
├─────────────────────────────────────────────────────────────┤
│              Infrastructure Layer (infrastructure)          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Plugin System (PF4J)                     │
│              plugin-sdk  ←──  slack-plugin                  │
└─────────────────────────────────────────────────────────────┘
```

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 25 + Virtual Threads |
| Framework | Spring Boot 4.0.1 |
| Plugin | PF4J 3.14.1 |
| Database | PostgreSQL / H2 |

## 시작하기

```bash
cd app/server
./gradlew bootRun
```

상세 실행 방법: [app/server/README.md](app/server/README.md)

## 문서

- [서버 가이드](app/server/README.md)
- [설계 문서](app/server/docs/DESIGN/)

## 라이선스

Apache License 2.0 - [LICENSE](LICENSE)
