# DOP Global Apps Server

## 로컬 개발 환경 설정

### 1. Docker PostgreSQL 실행

```bash
docker-compose up -d
```

또는 docker run 직접 실행:

```bash
docker run -d \
  --name global-apps-postgres \
  -e POSTGRES_DB=global_apps \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v global-apps-pgdata:/var/lib/postgresql/data \
  postgres:16-alpine
```

### 2. 서버 실행

```bash
./gradlew bootRun
```

### 3. 접속

- 서버: https://localhost:8443
- Slack OAuth: https://localhost:8443/slack/install

> 브라우저에서 "안전하지 않음" 경고 시 → "고급" → "localhost로 이동"

---

## Docker 관리

```bash
# 시작
docker-compose up -d

# 중지
docker-compose down

# 데이터 초기화 (볼륨 삭제)
docker-compose down -v

# 로그 확인
docker-compose logs -f postgres
```

---

## 빌드

```bash
# 빌드
./gradlew build

# 클린 빌드
./gradlew clean build

# 테스트 실행
./gradlew test
```

---

## 문서

- [SLACK_BOLT_INTEGRATION_PLAN.md](docs/SLACK_BOLT_INTEGRATION_PLAN.md) - Slack 통합 계획
- [SLACK_OAUTH_IMPLEMENTATION.md](docs/SLACK_OAUTH_IMPLEMENTATION.md) - OAuth 구현
- [SLACK_INTEGRATION_TEST.md](docs/SLACK_INTEGRATION_TEST.md) - 테스트 가이드
- [SLACK_PLUGIN_IMPLEMENTATION.md](docs/SLACK_PLUGIN_IMPLEMENTATION.md) - 플러그인 구현
