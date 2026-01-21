# 로컬 개발 환경 설정

## 개요

OAuth 연동(Slack, Jira 등)을 위해 로컬 환경에서 HTTPS가 필요합니다.
mkcert를 사용하여 브라우저 경고 없이 신뢰할 수 있는 로컬 인증서를 생성합니다.

---

## 사전 요구사항

| 도구 | 버전 | 용도 |
|------|------|------|
| Java | 25+ | 백엔드 서버 |
| Node.js | 20+ | 프론트엔드 |
| Docker | 최신 | PostgreSQL |
| mkcert | 최신 | SSL 인증서 |

---

## 빠른 시작

```bash
# 1. SSL 인증서 설정 (최초 1회)
./scripts/setup-local-ssl.sh

# 2. PostgreSQL 시작
cd app/server && docker-compose up -d

# 3. 백엔드 실행
./gradlew bootRun

# 4. 프론트엔드 실행 (새 터미널)
cd app/client && npm run dev
```

---

## 상세 설정 가이드

### 1. mkcert 설치

#### macOS

```bash
brew install mkcert
brew install nss  # Firefox 사용 시
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt install libnss3-tools
# mkcert 바이너리 다운로드
# https://github.com/FiloSottile/mkcert/releases
```

#### Windows

```powershell
choco install mkcert
# 또는 scoop install mkcert
```

### 2. SSL 인증서 생성

자동 스크립트 실행:

```bash
./scripts/setup-local-ssl.sh
```

스크립트 동작:
1. mkcert 설치 확인
2. 로컬 CA 시스템 등록 (최초 1회, 관리자 권한 필요)
3. localhost 인증서 생성 (`certs/` 디렉토리)
4. PKCS12 변환 (Spring Boot용)
5. Spring Boot 리소스에 복사

### 3. 생성되는 파일

```
project-root/
├── certs/                          # gitignore 대상
│   ├── localhost.pem              # 인증서
│   ├── localhost-key.pem          # 개인키
│   └── localhost.p12              # PKCS12 (Spring Boot용)
└── app/server/.../resources/
    └── localhost.p12              # 복사본
```

---

## 서비스 접속

| 서비스 | URL | 비고 |
|--------|-----|------|
| 백엔드 API | https://localhost:8443 | Spring Boot |
| 프론트엔드 | https://localhost:5173 | Vite (인증서 있을 때) |
| 프론트엔드 | http://localhost:5173 | Vite (인증서 없을 때) |
| PostgreSQL | localhost:5432 | Docker |

---

## OAuth 제공자별 Redirect URI 설정

각 OAuth 제공자의 앱 설정에서 아래 Redirect URI를 등록하세요.

| 제공자 | Redirect URI |
|--------|--------------|
| Slack | `https://localhost:8443/slack/oauth/callback` |
| Google | `https://localhost:8443/oauth/callback/google` |
| Microsoft | `https://localhost:8443/oauth/callback/microsoft` |
| Jira | `https://localhost:8443/oauth/callback/jira` |
| Zendesk | `https://localhost:8443/oauth/callback/zendesk` |

---

## 문제 해결

### mkcert 설치 후에도 브라우저 경고 발생

```bash
# CA 재설치
mkcert -uninstall
mkcert -install

# 브라우저 재시작 필요
```

### Firefox에서 인증서 인식 안됨

```bash
# nss 설치 필요
brew install nss  # macOS
sudo apt install libnss3-tools  # Linux
```

### 포트 충돌 (5432)

```bash
# 로컬 PostgreSQL 중지
brew services stop postgresql@15

# 사용 중인 프로세스 확인
lsof -i :5432
```

### 인증서 갱신

mkcert 인증서는 기본 2년 유효. 갱신 시:

```bash
# 기존 인증서 삭제 후 재생성
rm -rf certs/
./scripts/setup-local-ssl.sh
```

---

## 환경별 설정

### application-secret.yml (gitignore 대상)

```yaml
slack:
  app:
    client-id: "YOUR_CLIENT_ID"
    client-secret: "YOUR_CLIENT_SECRET"
    signing-secret: "YOUR_SIGNING_SECRET"
    scopes: "channels:history,channels:read,chat:write,..."
    redirect-uri: "https://localhost:8443/slack/oauth/callback"
```

### .env.development (프론트엔드)

```env
VITE_API_BASE_URL=https://localhost:8443
```

---

## 참고 자료

- [mkcert GitHub](https://github.com/FiloSottile/mkcert)
- [Slack OAuth 문서](https://api.slack.com/authentication/oauth-v2)
- [Google OAuth 문서](https://developers.google.com/identity/protocols/oauth2)
