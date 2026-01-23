# Issues

## [ISSUE-001] Plugin Execute API의 externalId 필수 구조 문제

### 현재 상황

- Plugin Execute API 호출 시 `externalId` (Slack의 경우 teamId)가 사실상 필수
- `externalId` 없이 호출하면 credential 조회 실패 → 401 에러 발생
- 에러 메시지가 "Access token required"로 불친절

### 문제점

1. **프론트엔드 부담 증가**
   - 프론트엔드가 `externalId`를 알고 있어야 함
   - OAuth 완료 후 연동 목록 조회 → teamId 저장 → API 호출 시 전달 필요

2. **명시적 검증 부재**
   - `PluginExecutorService.enrichWithCredential()`에서 externalId 누락 시 그냥 credential 없이 진행
   - 실제 에러는 `SlackPluginExecutor`에서 발생 (위치가 맞지 않음)

3. **DB 설계와 API 설계 불일치**
   - `plugin_connection` 테이블에 `company_id`, `user_id` 존재
   - 하지만 API에서는 이 컨텍스트를 활용하지 않음

### 현재 흐름

```
Client Request
    ↓
PluginExecutorService.execute()
    ↓
enrichWithCredential() → params에서 externalId 추출
    ↓
ConnectionService.getCredentialContext(pluginId, externalId)
    ↓
PluginExecutor.execute(request with credential)
```

### 관련 코드

- `PluginExecutorService.java:85-91` - externalId로 credential 조회
- `ConnectionService.java:206-210` - pluginId + externalId로 연동 조회
- `SlackPluginExecutor.java:59-61` - credential 없으면 401 에러

### 개선 방안

#### 방안 1: connectionId 사용

- 프론트엔드가 내부 PK(`connectionId`) 전달
- `externalId` 대신 `connectionId`로 직접 조회

```java
// 변경 전
connectionService.getCredentialContext(pluginId, externalId);

// 변경 후
connectionService.getCredentialContext(connectionId);
```

#### 방안 2: 사용자/회사 컨텍스트 활용

- 로그인 사용자의 `companyId` 또는 `userId`로 연동 조회
- `externalId` 파라미터 불필요

```java
// SecurityContext에서 사용자 정보 추출
Long companyId = SecurityUtils.getCurrentCompanyId();
connectionService.getCredentialContextByCompany(pluginId, companyId);
```

#### 방안 3: 단일 연동 가정 (간단한 경우)

- 플러그인당 하나의 연동만 존재한다고 가정
- `pluginId`만으로 조회

```java
connectionService.getFirstActiveCredential(pluginId);
```

### 결정 필요

- [ ] 어떤 방안을 적용할지 결정
- [ ] 다중 연동 지원 여부 확인
- [ ] 인증/인가 구조 (SecurityContext) 설계 확인
