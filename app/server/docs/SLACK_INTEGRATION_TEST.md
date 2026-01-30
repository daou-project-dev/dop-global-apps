# Slack 연동 테스트 가이드

## 사전 준비

### 1. Slack App 생성 및 설정

1. https://api.slack.com/apps 접속
2. "Create New App" → "From scratch"
3. 앱 이름, 워크스페이스 선택

### 2. OAuth & Permissions 설정

**Redirect URLs 추가**:
```
https://localhost:8443/slack/oauth/callback
```

**Bot Token Scopes 추가**:
- `channels:history`
- `channels:read`
- `chat:write`
- `chat:write.public` - 초대 없이 public 채널에 메시지 전송
- `commands`
- `app_mentions:read`
- `im:history`
- `im:read`
- `im:write`

### 3. 앱 자격 증명 복사

- **Client ID**: Basic Information → App Credentials
- **Client Secret**: Basic Information → App Credentials
- **Signing Secret**: Basic Information → App Credentials

---

## 로컬 환경 설정

### 1. Docker PostgreSQL 실행

```bash
cd app/server
docker-compose up -d
```

> 서버 재시작해도 토큰 유지됨 (H2 인메모리와 달리 데이터 영속)

### 2. application-secret.yml 생성

**경로**: `dop-gapps-server/src/main/resources/application-secret.yml`

```yaml
slack:
  app:
    client-id: "클라이언트ID"
    client-secret: "클라이언트시크릿"
    signing-secret: "서명시크릿"
    scopes: "channels:history,channels:read,chat:write,chat:write.public,commands,app_mentions:read,im:history,im:read,im:write"
    redirect-uri: "https://localhost:8443/slack/oauth/callback"
```

### 3. 서버 실행

```bash
cd app/server
./gradlew bootRun
```

---

## 테스트 절차

### Step 1: OAuth 플로우 (앱 설치)

브라우저에서 접속:
```
https://localhost:8443/slack/install
```

1. "이 사이트는 안전하지 않습니다" 경고 → "고급" → "localhost(안전하지 않음)으로 이동"
2. Slack 워크스페이스 선택 → "허용" 클릭
3. "Installation successful!" 메시지 확인

### Step 2: 채널 목록 조회

```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "slack",
    "method": "GET",
    "uri": "conversations.list",
    "teamId": "T워크스페이스ID",
    "body": "{}"
  }'
```

### Step 3: 메시지 전송 테스트

```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "slack",
    "method": "POST",
    "uri": "chat.postMessage",
    "teamId": "T워크스페이스ID",
    "body": "{\"channel\":\"C채널ID\",\"text\":\"Hello from local!\"}"
  }'
```

**성공 응답 예시**:
```json
{
  "success": true,
  "statusCode": 200,
  "body": "{\"ok\":true,\"channel\":\"C0A89167LHL\",\"ts\":\"1768886427.264759\"...}",
  "error": null
}
```

> `chat:write.public` scope가 있으면 봇 초대 없이도 public 채널에 메시지 전송 가능

---

## 트러블슈팅

### 1. Client ID가 과학적 표기법으로 변환됨

**증상**:
```
client_id=1.0296003183489104E13
```

**원인**: YAML에서 점(.)이 포함된 숫자가 소수로 파싱됨

**해결**: 따옴표로 감싸기
```yaml
client-id: "10296003183489.10283073624946"
```

---

### 2. redirect_uri did not match any configured URIs

**증상**:
```
redirect_uri did not match any configured URIs. Passed URI: http://localhost:8080/slack/oauth/callback
```

**원인**:
- Slack App에 Redirect URL 미등록
- 또는 HTTP 사용 (Slack은 HTTPS만 허용)

**해결**:
1. Slack App → OAuth & Permissions → Redirect URLs에 HTTPS URL 등록
2. 로컬에서 HTTPS 설정 (localhost.p12 인증서 사용)

---

### 3. SSL 인증서 오류 (ngrok)

**증상**:
```
failed to verify certificate: x509: certificate signed by unknown authority
```

**원인**: 회사 프록시가 SSL 트래픽을 가로채서 인증서 검증 실패

**해결**:
1. 회사 인증서를 Java truststore에 등록
2. 또는 로컬 HTTPS 설정으로 ngrok 우회

---

### 4. Jasypt 암호화 설정 누락

**증상**:
```
Error attempting to apply AttributeConverter: jasypt.encryptor.password must be provided
```

**원인**: Jasypt 암호화 키 미설정

**해결**: application-local.yml에 추가
```yaml
jasypt:
  encryptor:
    password: local
```

---

### 5. PKIX path building failed (Slack API 호출)

**증상**:
```
PKIX path building failed: unable to find valid certification path to requested target
```

**원인**: 회사 프록시 환경에서 Slack API 서버 인증서 검증 실패

**해결**: 회사 인증서를 Java truststore에 등록

```bash
# 프로젝트용 커스텀 truststore 생성
cp $JAVA_HOME/lib/security/cacerts src/main/resources/truststore.jks

keytool -importcert -alias daou-proxy \
  -keystore src/main/resources/truststore.jks \
  -storepass changeit \
  -file ~/Downloads/회사인증서.crt \
  -noprompt
```

build.gradle에 추가:
```gradle
bootRun {
    jvmArgs = [
        "-Djavax.net.ssl.trustStore=${projectDir}/src/main/resources/truststore.jks",
        "-Djavax.net.ssl.trustStorePassword=changeit"
    ]
}
```

---

### 6. channel_not_found

**증상**:
```json
{"success":false,"statusCode":400,"error":"channel_not_found"}
```

**원인**: 봇이 해당 채널에 초대되지 않음

**해결**:
1. `chat:write.public` scope 추가 (public 채널 한정)
2. 또는 Slack에서 `/invite @앱이름` 실행
3. 또는 채널 설정 → 통합 → 앱 추가

---

### 7. Token not found (서버 재시작 후)

**증상**:
```json
{"success":false,"statusCode":401,"error":"Token not found for teamId: TXXXXXX"}
```

**원인**: H2 인메모리 DB 사용 시 서버 재시작으로 토큰 초기화

**해결**: Docker PostgreSQL 사용
```bash
docker-compose up -d
```

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

## API 테스트 명령어 모음

### 채널 목록 조회
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "slack",
    "method": "GET",
    "uri": "conversations.list",
    "teamId": "TXXXXXX",
    "body": "{}"
  }'
```

### 메시지 전송
```bash
curl -k -s -X POST https://localhost:8443/execute \
  -H "Content-Type: application/json" \
  -d '{
    "plugin": "slack",
    "method": "POST",
    "uri": "chat.postMessage",
    "teamId": "TXXXXXX",
    "body": "{\"channel\":\"CXXXXXX\",\"text\":\"테스트 메시지\"}"
  }'
```

---

## 관련 문서

- [SLACK_OAUTH_IMPLEMENTATION.md](./SLACK_OAUTH_IMPLEMENTATION.md) - OAuth 구현 상세
- [SLACK_PLUGIN_IMPLEMENTATION.md](./SLACK_PLUGIN_IMPLEMENTATION.md) - 플러그인 구현
- [SLACK_BOLT_INTEGRATION_PLAN.md](./SLACK_BOLT_INTEGRATION_PLAN.md) - 전체 통합 계획
