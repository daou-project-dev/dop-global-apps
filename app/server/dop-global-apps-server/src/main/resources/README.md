# GAppBE 환경별 설정 가이드

## 설정 파일 구조

```
src/main/resources/
├── application.yml          # 공통 설정
├── application-local.yml    # 로컬 개발 환경 (H2)
├── application-dev.yml      # 개발 서버 환경 (PostgreSQL)
└── application-prod.yml     # 운영 환경 (PostgreSQL)
```

---

## 환경별 실행 방법

### 1. Local (로컬 개발)

**데이터베이스**: H2 인메모리
**특징**: 빠른 시작, 재시작 시 데이터 초기화

```bash
# Gradle
./gradlew bootRun --args='--spring.profiles.active=local'

# Java JAR
java -jar gappbe-server.jar --spring.profiles.active=local
```

**H2 콘솔 접속**:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:gappbe`
- Username: `sa`
- Password: (비어있음)

---

### 2. Dev (개발 서버)

**데이터베이스**: PostgreSQL
**특징**: 스키마 자동 업데이트 (ddl-auto: update)

```bash
# 환경 변수 설정
export DB_HOST=dev-postgres.example.com
export DB_PORT=5432
export DB_NAME=gappbe_dev
export DB_USERNAME=gappbe
export DB_PASSWORD=your-password

# 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**또는 인라인 설정**:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev --DB_HOST=localhost --DB_USERNAME=gappbe --DB_PASSWORD=dev123'
```

---

### 3. Prod (운영 환경)

**데이터베이스**: PostgreSQL
**특징**: 스키마 검증만 수행 (ddl-auto: validate)

```bash
# 환경 변수 설정 (필수)
export DB_HOST=prod-postgres.example.com
export DB_PORT=5432
export DB_NAME=gappbe
export DB_USERNAME=gappbe_prod
export DB_PASSWORD=secure-password
export ENCRYPTION_KEY=your-base64-encoded-256-bit-key

# 실행
java -jar gappbe-server.jar --spring.profiles.active=prod
```

---

## 환경별 설정 비교

| 항목 | Local | Dev | Prod |
|------|-------|-----|------|
| **데이터베이스** | H2 (인메모리) | PostgreSQL | PostgreSQL |
| **스키마 관리** | create-drop | update | validate |
| **SQL 로깅** | ✓ (DEBUG) | ✓ (DEBUG) | ✗ (WARN) |
| **로그 레벨** | DEBUG | DEBUG | INFO |
| **H2 콘솔** | ✓ | ✗ | ✗ |
| **커넥션 풀** | 기본 | 10 | 20 |
| **PF4J 모드** | development | development | deployment |

---

## 환경 변수

### 공통

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `ENCRYPTION_KEY` | (테스트용) | AES-256 암호화 키 (Base64) |
| `PF4J_PLUGINS_DIR` | `./plugins` | 플러그인 디렉토리 |
| `PF4J_MODE` | `development` | PF4J 모드 |

### Dev/Prod 전용

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `DB_HOST` | localhost (dev) / 필수 (prod) | PostgreSQL 호스트 |
| `DB_PORT` | 5432 | PostgreSQL 포트 |
| `DB_NAME` | gappbe_dev (dev) / gappbe (prod) | 데이터베이스명 |
| `DB_USERNAME` | gappbe (dev) / 필수 (prod) | DB 사용자명 |
| `DB_PASSWORD` | 필수 | DB 비밀번호 |

---

## 프로파일 전환 시 주의사항

### Local → Dev/Prod

1. PostgreSQL 서버 준비
2. 데이터베이스 생성
3. 환경 변수 설정
4. 스키마 마이그레이션 (Flyway 사용 권장)

### Dev → Prod

1. `ddl-auto: validate` 확인 (운영 DB 보호)
2. 암호화 키 변경 (운영용 키 사용)
3. 로그 레벨 확인 (민감 정보 노출 방지)

---

## 로그 레벨 커스터마이징

### 실행 시 오버라이드

```bash
# 특정 패키지 로그 레벨 변경
./gradlew bootRun --args='--spring.profiles.active=local --logging.level.com.daou.gappbe=TRACE'

# SQL 로깅 비활성화
./gradlew bootRun --args='--spring.profiles.active=local --logging.level.org.hibernate.SQL=WARN'
```

---

## 문제 해결

### H2 콘솔 접속 안됨
- 프로파일이 `local`인지 확인
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:gappbe` (정확히 입력)

### PostgreSQL 연결 실패
```bash
# 연결 테스트
psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME

# 환경 변수 확인
echo $DB_HOST $DB_PORT $DB_NAME $DB_USERNAME
```

### 포트 충돌
```bash
# 8080 포트 사용 중인 프로세스 종료
lsof -ti:8080 | xargs kill -9

# 또는 다른 포트 사용
./gradlew bootRun --args='--spring.profiles.active=local --server.port=8081'
```

---

## IDE 설정

### IntelliJ IDEA

1. **Run Configuration** 생성
2. **Program arguments**:
   - Local: `--spring.profiles.active=local`
   - Dev: `--spring.profiles.active=dev`
3. **Environment variables** (Dev/Prod):
   ```
   DB_HOST=localhost;DB_USERNAME=gappbe;DB_PASSWORD=dev123
   ```

### VS Code

`.vscode/launch.json`:
```json
{
  "configurations": [
    {
      "type": "java",
      "name": "GAppBE Local",
      "request": "launch",
      "mainClass": "com.daou.gappbe.server.GAppBeServerApplication",
      "args": "--spring.profiles.active=local"
    },
    {
      "type": "java",
      "name": "GAppBE Dev",
      "request": "launch",
      "mainClass": "com.daou.gappbe.server.GAppBeServerApplication",
      "args": "--spring.profiles.active=dev",
      "env": {
        "DB_HOST": "localhost",
        "DB_USERNAME": "gappbe",
        "DB_PASSWORD": "dev123"
      }
    }
  ]
}
```
