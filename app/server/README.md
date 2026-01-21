# DOP Global Apps Server

Spring Boot + PF4J 기반 플러그인 서버

## 모듈 구조

```
server/
├── dop-global-apps-core/           # 핵심 인터페이스 (Repository Port, StateStorage)
├── dop-global-apps-domain/         # 도메인 레이어 (Entity, Enum)
│   ├── enums/                      # AuthType, PluginStatus, ScopeType 등
│   ├── plugin/                     # Plugin Entity
│   ├── company/                    # Company Entity
│   ├── user/                       # User Entity
│   ├── connection/                 # PluginConnection Entity
│   └── credential/                 # OAuthCredential, ApiKeyCredential
├── dop-global-apps-infrastructure/ # 인프라 레이어 (core 인터페이스 구현)
│   ├── persistence/                # JPA Repository 구현체
│   ├── oauth/                      # StateStorage 구현체
│   └── crypto/                     # 암호화
├── dop-global-apps-api/            # API 레이어 (Entry Point)
│   ├── oauth/                      # OAuth 컨트롤러
│   ├── plugin/                     # 플러그인 서비스, 레지스트리
│   ├── connection/                 # 연동 관리 서비스
│   └── execute/                    # 플러그인 실행
└── plugins/
    ├── plugin-sdk/                 # 플러그인 SDK
    └── slack-plugin/               # Slack 연동 플러그인
```

## 의존성 구조 (Clean Architecture)

```
api -> core -> domain
         ↑
   infrastructure (core 인터페이스 구현)
```

## 로컬 개발 환경

### 1. H2 (기본)

```bash
./gradlew bootRun
```

### 2. PostgreSQL (Docker)

```bash
# PostgreSQL 실행
docker compose up -d

# 서버 실행 (dev 프로파일)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 3. 환경 변수

```bash
# Slack OAuth (필수)
export SLACK_CLIENT_ID=<your-client-id>
export SLACK_CLIENT_SECRET=<your-client-secret>
export SLACK_SIGNING_SECRET=<your-signing-secret>

# 암호화 키 (운영 환경 필수)
export JASYPT_ENCRYPTOR_PASSWORD=<encryption-key>
```

### 4. 접속

| 환경 | URL |
|------|-----|
| 로컬 (HTTP) | http://localhost:8080 |
| 로컬 (HTTPS) | https://localhost:8443 |
| Slack OAuth | https://localhost:8443/oauth/slack/install |

## 빌드

```bash
# 빌드
./gradlew build

# 클린 빌드
./gradlew clean build

# 테스트 제외 빌드
./gradlew build -x test

# 테스트 실행
./gradlew test
```

## Docker 관리

```bash
# 시작
docker compose up -d

# 중지
docker compose down

# 데이터 초기화 (볼륨 삭제)
docker compose down -v

# 로그 확인
docker compose logs -f postgres
```

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/oauth/{pluginId}/install` | OAuth 설치 시작 |
| GET | `/oauth/{pluginId}/callback` | OAuth 콜백 |
| POST | `/api/execute` | 플러그인 API 실행 |

## 설계 문서

- [도메인 모델](docs/DESIGN/DOMAIN.md)
- [백엔드 레이어](docs/DESIGN/BACKEND_LAYER.md)
- [플러그인 아키텍처](docs/DESIGN/PLUGIN.md)
- [구현 로드맵](docs/DESIGN/IMPLEMENTATION_ROADMAP.md)

## 레거시 문서

- [Slack OAuth 구현](docs/SLACK_OAUTH_IMPLEMENTATION.md)
- [Slack 플러그인 구현](docs/SLACK_PLUGIN_IMPLEMENTATION.md)
- [통합 테스트 가이드](docs/SLACK_INTEGRATION_TEST.md)
