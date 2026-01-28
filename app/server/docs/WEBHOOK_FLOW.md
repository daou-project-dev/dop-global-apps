# 웹훅 비즈니스 흐름

## 전체 라이프사이클

```mermaid
flowchart TB
    subgraph Phase1["1️⃣ 연동 설정"]
        A[사용자] -->|Jira 연동 클릭| B[OAuth 인증]
        B -->|access_token<br/>refresh_token<br/>cloudId| C[(DB 저장)]
        C -->|connection_id 생성| D[웹훅 URL 발급]
        D -->|/webhook/jira/123| E[사용자에게 URL 표시]
    end

    subgraph Phase2["2️⃣ Jira 웹훅 등록"]
        E -->|URL 복사| F[Jira 설정 페이지]
        F -->|웹훅 URL 등록<br/>이벤트 선택| G[Jira 웹훅 활성화]
    end

    subgraph Phase3["3️⃣ 이벤트 발생 및 처리"]
        H[이슈 생성/수정] -->|Jira가 웹훅 전송| I[서버 수신]
        I -->|connectionId로<br/>connection 조회| J[이벤트 파싱]
        J -->|구독자에게 전달| K[후속 처리]
    end

    G -.->|이벤트 발생 시| H
```

---

## Phase 1: 연동 설정 상세

```mermaid
sequenceDiagram
    autonumber
    participant User as 사용자
    participant App as 우리 앱
    participant Jira as Jira Cloud
    participant DB as Database

    User->>App: Jira 연동 버튼 클릭

    App->>Jira: OAuth 인증 URL로 리다이렉트
    Jira->>User: 로그인 및 권한 동의 화면
    User->>Jira: 권한 승인
    Jira->>App: callback (authorization_code)

    App->>Jira: 토큰 교환 요청
    Jira-->>App: access_token, refresh_token, expires_in

    App->>Jira: accessible-resources 조회
    Jira-->>App: cloudId, siteUrl, siteName

    Note over App,DB: 저장되는 정보

    App->>DB: plugin_connection 저장
    Note right of DB: id: 123<br/>plugin_id: "jira"<br/>external_id: "mycompany"<br/>external_name: "MyCompany Jira"<br/>metadata: {cloudId: "xxx"}

    App->>DB: oauth_credential 저장
    Note right of DB: connection_id: 123<br/>access_token: "eyJ..."<br/>refresh_token: "eyJ..."<br/>expires_at: 1시간 후

    App-->>User: 연동 완료!<br/>웹훅 URL: /webhook/jira/123
```

### 저장되는 데이터

| 테이블 | 필드 | 값 예시 | 용도 |
|--------|------|---------|------|
| `plugin_connection` | id | 123 | 웹훅 URL에 사용 |
| | plugin_id | "jira" | 플러그인 식별 |
| | external_id | "mycompany" | Jira 도메인 |
| | external_name | "MyCompany Jira" | 표시용 이름 |
| | metadata | {"cloudId": "xxx"} | API 호출 시 필요 |
| `oauth_credential` | access_token | "eyJ..." | API 인증 |
| | refresh_token | "eyJ..." | 토큰 갱신용 |
| | expires_at | 2025-01-28 15:00 | 만료 시점 |

---

## Phase 2: Jira에서 웹훅 등록

```mermaid
sequenceDiagram
    autonumber
    participant User as 사용자
    participant App as 우리 앱
    participant Jira as Jira 설정

    User->>App: 웹훅 URL 복사<br/>/webhook/jira/123

    User->>Jira: 설정 > 시스템 > 웹훅

    User->>Jira: 웹훅 생성
    Note right of Jira: URL: https://our-domain.com/webhook/jira/123<br/>이벤트: issue_created, issue_updated<br/>JQL 필터: project = MYPROJ (선택)

    Jira-->>User: 웹훅 등록 완료

    Note over User,Jira: 이제 이슈 변경 시 웹훅이 전송됨
```

---

## Phase 3: 웹훅 수신 및 처리

```mermaid
sequenceDiagram
    autonumber
    participant Jira as Jira Cloud
    participant Server as 우리 서버
    participant DB as Database
    participant Sub as 구독자

    Note over Jira: 이슈 생성됨

    Jira->>Server: POST /webhook/jira/123<br/>{"webhookEvent": "jira:issue_created", ...}

    Server->>Server: URL에서 connectionId 추출 (123)

    Server->>DB: connection 조회 (id=123)
    DB-->>Server: plugin_id: "jira"<br/>company_id: 1<br/>metadata: {cloudId: "xxx"}

    Server->>Server: 서명 검증 (선택적)
    Server->>Server: 이벤트 파싱

    Note over Server: 파싱된 이벤트
    Note right of Server: pluginId: "jira"<br/>eventType: "jira:issue_created"<br/>connectionId: 123<br/>companyId: 1<br/>data: {issue: {...}}

    Server->>DB: 이벤트 로그 저장

    Server->>DB: 매칭되는 구독 조회
    DB-->>Server: 구독 목록

    loop 각 구독
        Server->>Sub: 이벤트 전달 (HTTP/내부)
        Sub-->>Server: OK
    end

    Server-->>Jira: 200 OK
```

---

## 토큰 자동 갱신 흐름

```mermaid
sequenceDiagram
    autonumber
    participant Client as 클라이언트
    participant Server as 우리 서버
    participant DB as Database
    participant Jira as Jira API

    Client->>Server: Jira 이슈 조회 요청

    Server->>DB: credential 조회
    DB-->>Server: access_token, expires_at

    Server->>Server: 토큰 만료 확인

    alt 토큰 유효
        Server->>Jira: API 호출 (기존 토큰)
    else 토큰 만료
        Server->>Jira: 토큰 갱신 요청<br/>(refresh_token 사용)
        Jira-->>Server: 새 access_token<br/>expires_in: 3600

        Server->>DB: 새 토큰 저장<br/>expires_at 업데이트

        Server->>Jira: API 호출 (새 토큰)
    end

    Jira-->>Server: API 응답
    Server-->>Client: 결과 반환
```

---

## 데이터 흐름 요약

```mermaid
flowchart LR
    subgraph 연동시["연동 시 저장"]
        A1[access_token] --> DB1[(oauth_credential)]
        A2[refresh_token] --> DB1
        A3[expires_at] --> DB1
        B1[cloudId] --> DB2[(plugin_connection)]
        B2[external_id] --> DB2
        B3[company_id] --> DB2
    end

    subgraph 웹훅수신["웹훅 수신 시"]
        C1[connectionId from URL] --> MATCH{매칭}
        DB2 --> MATCH
        MATCH --> D1[company_id 확인]
        MATCH --> D2[cloudId로 API 호출 가능]
    end

    subgraph API호출["API 호출 시"]
        E1[요청] --> CHECK{토큰 만료?}
        CHECK -->|Yes| REFRESH[갱신]
        REFRESH --> DB1
        CHECK -->|No| CALL[API 호출]
        REFRESH --> CALL
        DB1 --> CALL
        DB2 -->|cloudId| CALL
    end
```

---

## 핵심 매핑 관계

```
┌─────────────────────────────────────────────────────────────────┐
│                         웹훅 URL                                 │
│                                                                 │
│    /webhook/jira/123                                            │
│                   │                                             │
│                   └──▶ connectionId = 123                       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                     plugin_connection                           │
│                                                                 │
│    id=123 ──▶ plugin_id="jira"                                  │
│           ──▶ external_id="mycompany" (Jira 도메인)             │
│           ──▶ company_id=1 (우리 시스템 고객사)                  │
│           ──▶ metadata.cloudId="xxx" (Jira API용)               │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                     oauth_credential                            │
│                                                                 │
│    connection_id=123 ──▶ access_token (API 호출용)              │
│                      ──▶ refresh_token (갱신용)                 │
│                      ──▶ expires_at (만료 시점)                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2025-01-28 | 초안 작성 |
