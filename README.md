# DOP Global Apps

다우오피스 글로벌 앱 통합 프로젝트

## 프로젝트 구조

```
dop-global-apps/
├── app/
│   └── server/                      # 백엔드 (Spring Boot + PF4J)
│       ├── dop-global-apps-core/    # 공통 유틸리티 및 핵심 로직
│       ├── dop-global-apps-server/  # Entry Point (Boot 애플리케이션)
│       └── plugins/                 # 플러그인 모듈
│           └── slack-plugin/
├── docs/                            # 공통 문서
├── .claude/                         # Claude Code 설정 및 문서
├── LICENSE
└── README.md
```

## 서버 실행

### Local 환경 (H2)
```bash
cd app/server
./gradlew bootRun
```

### Dev 환경 (PostgreSQL)
```bash
cd app/server
export DB_HOST=localhost
export DB_USERNAME=gappbe
export DB_PASSWORD=dev123
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 기술 스택

### Backend (app/server)
- Java 25
- Spring Boot 4.0.1
- Virtual Threads (Project Loom)
- PF4J 3.14.1 (플러그인 시스템)
- PostgreSQL / H2
- Hibernate 7.2.0

### 주요 기능
- OAuth 2.0 인증 (Slack, Gmail, M365 등)
- API Key 인증
- 게이트웨이 프록시
- 플러그인 기반 확장

## 문서

상세 문서는 `.claude/docs/` 디렉토리 참조:
- [구현 가이드](/.claude/docs/GAPPBE_IMPLEMENTATION.md)
- [실행 계획](/.claude/docs/IMPLEMENTATION_PLAN.md)
- [PF4J 아키텍처](/.claude/docs/PF4J_ARCHITECTURE.md)
- [프로젝트 구조](/.claude/docs/PROJECT_STRUCTURE.md)

## 라이선스

Apache License 2.0 - [LICENSE](LICENSE) 참조
