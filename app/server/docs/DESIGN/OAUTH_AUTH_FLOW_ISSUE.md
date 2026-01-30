# OAuth 인증 플로우 설계 이슈

## 개요

플러그인 OAuth 인증 시작 URL 처리가 프론트엔드에 하드코딩되어 있어 확장성 문제 발생.

## 현재 구조

### 프론트엔드 (`plugin-auth-page.tsx`)

```typescript
const SLACK_PLUGIN_ID = 'slack';

const handleSubmit = () => {
  // ❌ 문제: pluginId별 하드코딩 분기
  if (currentPlugin.pluginId === SLACK_PLUGIN_ID && currentPlugin.authType === 'oAuth2') {
    window.open(`${API_BASE_URL}/oauth/${currentPlugin.pluginId}/install`, ...);
  } else if (currentPlugin.pluginId === 'ms365-calendar' && currentPlugin.authType === 'oAuth2') {
    // TODO: 구현 필요
  } else {
    alert('저장 기능 준비 중');
  }
};
```

### 백엔드 데이터 흐름

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Backend                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Plugin Entity (DB)              PluginMetadata (SDK)               │
│  ┌─────────────────┐            ┌─────────────────────┐             │
│  │ pluginId        │            │ pluginId            │             │
│  │ name            │            │ name                │             │
│  │ authType        │            │ authType            │             │
│  │ metadata (JSON) │───────────▶│ authUrl        ◀────│── OAuth URL │
│  │   - scopes      │            │ tokenUrl            │             │
│  │   - apiBaseUrl  │            │ defaultScopes       │             │
│  └─────────────────┘            └─────────────────────┘             │
│           │                                                          │
│           ▼                                                          │
│  PluginResponse (API 응답)                                          │
│  ┌─────────────────┐                                                │
│  │ pluginId        │                                                │
│  │ name            │                                                │
│  │ authType        │  ❌ OAuth URL 정보 미포함                      │
│  │ iconUrl         │                                                │
│  └─────────────────┘                                                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Frontend                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Plugin 타입                     현재 처리 방식                      │
│  ┌─────────────────┐            ┌─────────────────────────────┐     │
│  │ pluginId        │            │ if (pluginId === 'slack')   │     │
│  │ name            │───────────▶│   → /oauth/slack/install    │     │
│  │ authType        │            │ else if (pluginId === ...)  │     │
│  │                 │            │   → 하드코딩 분기 추가 필요  │     │
│  │ ❌ authUrl 없음 │            └─────────────────────────────┘     │
│  └─────────────────┘                                                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

## 문제점

| 구분 | 문제 | 영향 |
|------|------|------|
| 확장성 | 새 OAuth 플러그인 추가 시 프론트엔드 코드 수정 필요 | 개발 비용 증가 |
| 유지보수 | pluginId 하드코딩으로 타입 안정성 없음 | 오류 가능성 |
| 일관성 | 백엔드에 OAuth 정보 있으나 프론트엔드에 전달 안 됨 | 데이터 불일치 |

## 관련 코드 위치

### 프론트엔드

| 파일 | 역할 |
|------|------|
| `client/src/pages/plugin-auth/plugin-auth-page.tsx` | OAuth 인증 시작 (하드코딩) |
| `client/src/store/types.ts` | Plugin 타입 정의 (authUrl 없음) |

### 백엔드

| 파일 | 역할 |
|------|------|
| `server/plugins/plugin-sdk/.../PluginMetadata.java` | OAuth URL 정보 보유 |
| `server/dop-gapps-domain/.../Plugin.java` | metadata JSON에 OAuth 정보 저장 |
| `server/dop-gapps-api/.../PluginResponse.java` | API 응답 (OAuth URL 미포함) |

## 개선 방안

### Option A: API 응답에 OAuth 정보 추가

**백엔드 수정**:
```java
// PluginResponse.java
public record PluginResponse(
    String pluginId,
    String name,
    String authType,
    String iconUrl,
    boolean active,
    // 추가
    String oauthInstallUrl  // "/oauth/{pluginId}/install" 또는 null
) {}
```

**프론트엔드 수정**:
```typescript
// types.ts
interface Plugin {
  pluginId: string;
  name: string;
  authType: AuthType;
  // 추가
  oauthInstallUrl?: string;
}

// plugin-auth-page.tsx
const handleSubmit = () => {
  if (plugin.oauthInstallUrl) {
    window.open(plugin.oauthInstallUrl, 'plugin-oauth', ...);
  }
};
```

### Option B: authType 기반 URL 패턴 통일

모든 OAuth 플러그인이 동일한 URL 패턴 사용:
```
/oauth/{pluginId}/install
```

**프론트엔드 수정**:
```typescript
const handleSubmit = () => {
  if (currentPlugin.authType === 'OAUTH2') {
    window.open(`${API_BASE_URL}/oauth/${currentPlugin.pluginId}/install`, ...);
  }
};
```

### 권장: Option B

- 백엔드 수정 최소화
- URL 패턴 통일로 일관성 확보
- 프론트엔드 하드코딩 제거

## TODO

- [ ] 프론트엔드 `handleSubmit` 로직 수정 (authType 기반 분기)
- [ ] 프론트엔드 Plugin 타입에 authType 추가 확인
- [ ] 백엔드 `/oauth/{pluginId}/install` 엔드포인트 ms365-calendar 지원 확인
