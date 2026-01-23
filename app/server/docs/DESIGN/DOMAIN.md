# 도메인 설계

## 개요

다양한 외부 서비스(Slack, Google, MS 365 등)와 연동하기 위한 플러그인 시스템의 도메인 설계

---

## 1. 요구사항

### 1.1 지원 범위

| 구분 | 내용 |
|------|------|
| **플러그인** | Slack, Google Workspace, MS 365, Jira, Teams, Notion |
| **인증 방식** | OAuth2, API Key |

### 1.2 연동 단위

```
┌─────────────────────────────────────────────────────────┐
│  Company (고객사)                                       │
├─────────────────────────────────────────────────────────┤
│  ├── Workspace 연동 (Slack, Jira, Notion 등)            │
│  │   └── 워크스페이스 A, B, C...                        │
│  │                                                      │
│  └── User 연동 (Gmail,111Google Calendar 등)              │
│      └── 사용자 1, 2, 3...                              │
└─────────────────────────────────────────────────────────┘
```

- 고객사 하나가 여러 워크스페이스 사용 가능
- Gmail 등은 개별 사용자별 연동 관리

### 1.3 기능 범위

| 기능 | 현재 | 추후 |
|------|:----:|:----:|
| API 호출 (Outbound) | ✅ | ✅ |
| 이벤트 수신 (Inbound) | - | ✅ |
| 토큰 자동 갱신 | - | ✅ |
| 연동 해제/재연동 | - | ✅ |
| 관리 UI | - | ✅ |

### 1.4 프로비저닝

- `company.company_uid`: 외부 서비스에서 프로비저닝
- `user.platform_user_id`: 외부 서비스에서 프로비저닝

---

## 2. 테이블 설계

### 2.1 ERD

```
┌──────────────────────────┐
│ plugin (플러그인 마스터) │
├──────────────────────────┤
│  id            BIGINT PK │
│  plugin_id     VARCHAR UK│
│  name          VARCHAR   │
│  description   VARCHAR   │
│  auth_type     VARCHAR   │
│  client_id     VARCHAR   │
│  client_secret VARCHAR   │  ◀── 암호화
│  secrets       JSON      │  ◀── 암호화
│  metadata      JSON      │
│  icon_url      VARCHAR   │
│  status        VARCHAR   │
│  created_at    TIMESTAMP │
│  updated_at    TIMESTAMP │
└──────────────────────────┘
             │
             │ (논리적 참조)
             │
             │         ┌──────────────────────────┐
             │         │  company (고객사)        │
             │         ├──────────────────────────┤
             │         │  id            BIGINT PK │
             │         │  company_uid   VARCHAR UK│  ◀── 프로비저닝
             │         │  name          VARCHAR   │
             │         │  status        VARCHAR   │
             │         │  created_at    TIMESTAMP │
             │         │  updated_at    TIMESTAMP │
             │         └──────────────────────────┘
             │                      │
             │                      │ 1:N (논리적 참조)
             │                      ▼
             │         ┌──────────────────────────┐
             │         │  user (사용자)           │
             │         ├──────────────────────────┤
             │         │  id            BIGINT PK │
             │         │  company_id    BIGINT    │
             │         │  platform_user_id VARCHAR│  ◀── 프로비저닝
             │         │  login_id      VARCHAR   │
             │         │  name          VARCHAR   │
             │         │  email         VARCHAR   │
             │         │  status        VARCHAR   │
             │         │  created_at    TIMESTAMP │
             │         │  updated_at    TIMESTAMP │
             │         └──────────────────────────┘
             │                      │
             │ (논리적 참조)         │ (논리적 참조)
             │                      │
             │    ┌─────────────────┴────────────────┐
             │    │                                  │
             ▼    ▼                                  │
     ┌──────────────────────────┐                   │
     │  plugin_connection       │◀──────────────────┘
     │  (연동 정보)             │
     ├──────────────────────────┤
     │  id            BIGINT PK │
     │  plugin_id     VARCHAR   │ ─── plugin.plugin_id
     │  company_id    BIGINT    │ ─── company.id
     │  user_id       BIGINT    │ ─── user.id (USER 타입)
     │  scope_type    VARCHAR   │
     │  external_id   VARCHAR   │
     │  external_name VARCHAR   │
     │  metadata      JSON      │
     │  status        VARCHAR   │
     │  created_at    TIMESTAMP │
     │  updated_at    TIMESTAMP │
     └──────────────────────────┘
                  │
                  │ 1:1
   ┌──────────────┴──────────────┐
   ▼                             ▼
┌─────────────────────┐   ┌─────────────────────┐
│  oauth_credential   │   │  apikey_credential  │
├─────────────────────┤   ├─────────────────────┤
│  id           PK    │   │  id           PK    │
│  connection_id UK   │   │  connection_id UK   │
│  access_token       │   │  api_key            │
│  refresh_token      │   │  api_secret         │
│  scope              │   │  created_at         │
│  expires_at         │   │  updated_at         │
│  created_at         │   └─────────────────────┘
│  updated_at         │
└─────────────────────┘

* FK 제약 조건 없음 (논리적 참조만 유지)
* 암호화 대상: client_secret, secrets, access_token, refresh_token, api_key, api_secret
```

### 2.2 테이블 관계

| 관계 | 설명 |
|------|------|
| company → user | 1:N (논리적, 고객사에 여러 사용자) |
| plugin → plugin_connection | 1:N (논리적, 하나의 플러그인에 여러 연동) |
| company → plugin_connection | 1:N (논리적, 하나의 고객사에 여러 연동) |
| user → plugin_connection | 1:N (논리적, USER 타입일 때 사용자별 연동) |
| plugin_connection → oauth_credential | 1:1 (auth_type이 OAUTH2일 때) |
| plugin_connection → apikey_credential | 1:1 (auth_type이 API_KEY일 때) |

### 2.3 테이블 상세

#### plugin (플러그인 마스터)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| plugin_id | VARCHAR(50) | UK, NOT NULL | 플러그인 식별자 ("slack", "google" 등) |
| name | VARCHAR(100) | NOT NULL | 표시명 ("Slack", "Google Workspace") |
| description | VARCHAR(500) | | 플러그인 설명 |
| auth_type | VARCHAR(20) | NOT NULL | OAUTH2, API_KEY |
| client_id | VARCHAR(200) | | OAuth Client ID |
| client_secret | VARCHAR(500) | | OAuth Client Secret (암호화) |
| secrets | JSON | | 플러그인별 추가 민감 정보 (암호화) |
| metadata | JSON | | OAuth URL, scopes 등 일반 설정 |
| icon_url | VARCHAR(500) | | 아이콘 URL |
| status | VARCHAR(20) | NOT NULL | ACTIVE, INACTIVE |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | | 수정일시 |

#### company (고객사)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| company_uid | VARCHAR(50) | UK, NOT NULL | 고객사 고유 식별자 (프로비저닝) |
| name | VARCHAR(100) | NOT NULL | 고객사명 |
| status | VARCHAR(20) | NOT NULL | ONLINE, OFFLINE 등 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | | 수정일시 |

#### user (사용자)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| company_id | BIGINT | NOT NULL | 소속 고객사 (논리적 참조) |
| platform_user_id | VARCHAR(100) | NOT NULL | 플랫폼 사용자 ID (프로비저닝) |
| login_id | VARCHAR(100) | | 로그인 ID |
| name | VARCHAR(100) | | 사용자명 |
| email | VARCHAR(200) | | 이메일 |
| status | VARCHAR(20) | NOT NULL | ACTIVE, INACTIVE |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | | 수정일시 |

**인덱스**:
- UK: `(company_id, platform_user_id)`

#### plugin_connection (연동 정보)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| plugin_id | VARCHAR(50) | NOT NULL | plugin.plugin_id (논리적 참조) |
| company_id | BIGINT | NOT NULL | company.id (논리적 참조) |
| user_id | BIGINT | | user.id (USER 타입일 때, 논리적 참조) |
| scope_type | VARCHAR(20) | NOT NULL | WORKSPACE, USER |
| external_id | VARCHAR(100) | | 외부 시스템 ID |
| external_name | VARCHAR(200) | | 표시용 이름 |
| metadata | JSON | | 연동별 추가 정보 |
| status | VARCHAR(20) | NOT NULL | ACTIVE, REVOKED |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | | 수정일시 |

**인덱스**:
- UK: `(plugin_id, company_id, external_id, user_id)`

#### oauth_credential (OAuth 인증 정보)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| connection_id | BIGINT | UK, NOT NULL | plugin_connection.id (논리적 참조) |
| access_token | VARCHAR(1024) | NOT NULL | 액세스 토큰 (암호화) |
| refresh_token | VARCHAR(1024) | | 리프레시 토큰 (암호화) |
| scope | VARCHAR(500) | | 권한 범위 |
| expires_at | TIMESTAMP | | 토큰 만료일시 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | | 수정일시 |

#### apikey_credential (API Key 인증 정보)

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| connection_id | BIGINT | UK, NOT NULL | plugin_connection.id (논리적 참조) |
| api_key | VARCHAR(512) | NOT NULL | API Key (암호화) |
| api_secret | VARCHAR(512) | | API Secret (암호화) |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | | 수정일시 |

### 2.4 데이터 예시

#### plugin 테이블

| plugin_id | name | auth_type | client_id | status |
|-----------|------|-----------|-----------|--------|
| slack | Slack | OAUTH2 | 123456.789012 | ACTIVE |
| google | Google Workspace | OAUTH2 | xxx.apps.googleusercontent.com | ACTIVE |
| ms365 | Microsoft 365 | OAUTH2 | xxxxxxxx-xxxx-xxxx-xxxx | ACTIVE |
| jira | Jira | OAUTH2 | jira-client-id | ACTIVE |
| notion | Notion | API_KEY | NULL | ACTIVE |
| teams | Microsoft Teams | OAUTH2 | xxxxxxxx-xxxx-xxxx-xxxx | INACTIVE |

#### plugin.secrets 예시

```json
// Slack
{
  "signing_secret": "abc123..."
}

// Google (추가 secret 없음)
{}

// Jira
{
  "webhook_secret": "xyz789..."
}
```

#### plugin.metadata 예시

```json
// Slack
{
  "oauth": {
    "authorization_url": "https://slack.com/oauth/v2/authorize",
    "token_url": "https://slack.com/api/oauth.v2.access",
    "scopes": "channels:read,channels:history,chat:write,chat:write.public"
  },
  "api_base_url": "https://slack.com/api"
}

// Google Workspace
{
  "oauth": {
    "authorization_url": "https://accounts.google.com/o/oauth2/v2/auth",
    "token_url": "https://oauth2.googleapis.com/token",
    "scopes": "https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/gmail.send"
  },
  "api_base_url": "https://www.googleapis.com"
}

// Microsoft 365
{
  "oauth": {
    "authorization_url": "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
    "token_url": "https://login.microsoftonline.com/common/oauth2/v2.0/token",
    "scopes": "https://graph.microsoft.com/Calendars.ReadWrite https://graph.microsoft.com/Mail.Send"
  },
  "api_base_url": "https://graph.microsoft.com/v1.0"
}

// Notion (API Key)
{
  "api_base_url": "https://api.notion.com/v1",
  "api_version": "2022-06-28"
}
```

#### company 테이블

| id | company_uid | name | status |
|----|-------------|------|--------|
| 1 | COM-A001 | A사 | ONLINE |
| 2 | COM-B002 | B사 | ONLINE |

#### user 테이블

| id | company_id | platform_user_id | login_id | name | email |
|----|------------|------------------|----------|------|-------|
| 1 | 1 | USR-001 | kim.cs | 김철수 | kim@a.com |
| 2 | 1 | USR-002 | lee.yh | 이영희 | lee@a.com |
| 3 | 2 | USR-003 | park.mj | 박민준 | park@b.com |

#### plugin_connection 테이블

| company_id | plugin_id | scope_type | external_id | user_id | 설명 |
|------------|-----------|------------|-------------|---------|------|
| 1 | slack | WORKSPACE | T0A8Q035 | NULL | A사 Slack 워크스페이스 |
| 1 | slack | WORKSPACE | T1B9R146 | NULL | A사 두번째 Slack |
| 1 | google | USER | - | 1 | A사 김철수 Gmail |
| 1 | google | USER | - | 2 | A사 이영희 Gmail |
| 2 | jira | WORKSPACE | jira-123 | NULL | B사 Jira |
| 2 | notion | WORKSPACE | - | NULL | B사 Notion (API Key) |

#### plugin_connection.metadata 예시

```json
// Slack 연동
{
  "bot_user_id": "U0AA3GAUSSD",
  "app_id": "A0A8B25JCTU",
  "incoming_webhook_url": "https://hooks.slack.com/services/..."
}

// Google Calendar 연동
{
  "calendar_id": "primary",
  "sync_token": "..."
}
```

---

## 3. Enum 정의

### 3.1 AuthType (인증 방식)

```java
public enum AuthType {
    OAUTH2,     // OAuth 2.0 인증
    API_KEY     // API Key 인증
}
```

### 3.2 ScopeType (연동 범위)

```java
public enum ScopeType {
    WORKSPACE,  // 워크스페이스/조직 단위
    USER        // 사용자 단위
}
```

### 3.3 ConnectionStatus (연동 상태)

```java
public enum ConnectionStatus {
    ACTIVE,     // 활성
    REVOKED     // 해제됨
}
```

### 3.4 PluginStatus (플러그인 상태)

```java
public enum PluginStatus {
    ACTIVE,     // 활성 (사용 가능)
    INACTIVE    // 비활성 (사용 불가)
}
```

### 3.5 CompanyStatus (고객사 상태)

```java
public enum CompanyStatus {
    ONLINE,     // 정상
    OFFLINE     // 비활성
    // 추후 확장 가능
}
```

### 3.6 UserStatus (사용자 상태)

```java
public enum UserStatus {
    ACTIVE,     // 활성
    INACTIVE    // 비활성
}
```

---

## 4. 설계 원칙

### 4.1 FK 미사용

- DB 레벨 FK 제약 조건 없음
- 논리적 참조만 유지
- 애플리케이션 레벨에서 무결성 검증

**장점**:
- 마이크로서비스 환경에서 유연성
- 데이터 마이그레이션 용이
- FK 체크 오버헤드 없음
- 테이블 간 독립적 관리 가능

### 4.2 VARCHAR Enum

- DB에서 ENUM 타입 대신 VARCHAR 사용
- JPA `@Enumerated(EnumType.STRING)`으로 변환

**장점**:
- DB 이식성 (MySQL, PostgreSQL, H2 모두 호환)
- Enum 값 추가 시 DB 스키마 변경 불필요

### 4.3 프로비저닝

- `company_uid`, `platform_user_id`는 외부 서비스에서 프로비저닝
- 내부 PK (`id`)와 외부 식별자 분리

### 4.4 암호화

암호화 대상 컬럼:

| 테이블 | 컬럼 | 암호화 방식 |
|--------|------|------------|
| plugin | client_secret | JPA Converter |
| plugin | secrets | JSON 전체 암호화 |
| oauth_credential | access_token | JPA Converter |
| oauth_credential | refresh_token | JPA Converter |
| apikey_credential | api_key | JPA Converter |
| apikey_credential | api_secret | JPA Converter |

---

## 5. 변경 이력

| 날짜 | 버전 | 내용 |
|------|------|------|
| 2025-01-21 | 0.1 | 초안 작성 |
| 2025-01-21 | 0.2 | plugin 마스터 테이블 추가, FK 제약 조건 제거 |
| 2025-01-21 | 0.3 | user 테이블 추가, 프로비저닝 정보 추가 |
| 2025-01-21 | 0.4 | plugin/plugin_connection에 metadata, secrets 컬럼 추가 |
| 2025-01-21 | 0.5 | 문서 분리 (DESIGN/DOMAIN.md) |
