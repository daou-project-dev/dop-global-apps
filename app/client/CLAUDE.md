# Client CLAUDE.md

프론트엔드 클라이언트 개발 가이드

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | React | 19.x |
| Language | TypeScript | 5.9.x |
| Build Tool | Vite | 7.x |
| 데이터 페칭 | TanStack Query | 5.x |
| 상태관리 | Jotai | 2.x |
| 불변성 | Immer | 11.x |
| 스타일링 | CSS Modules | - |
| 유틸리티 | lodash, clsx | - |

## IDE 설정 (VS Code)

### 필수 확장 프로그램

- **ESLint** (`dbaeumer.vscode-eslint`)
- **Prettier** (`esbenp.prettier-vscode`)

### 설정 방법

`Cmd + Shift + P` → `Preferences: Open User Settings (JSON)` → 아래 내용 추가:

```json
{
  // 저장 시 자동 포맷팅
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",

  // 저장 시 ESLint 자동 수정
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": "explicit"
  },

  // ESLint 활성화
  "eslint.enable": true,
  "eslint.validate": ["javascript", "javascriptreact", "typescript", "typescriptreact"],

  // TypeScript/JavaScript 전용 설정
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[typescriptreact]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  }
}
```

### 검증

설정 후 `.tsx` 파일 저장 시:
- Prettier 포맷팅 자동 적용
- ESLint 에러 자동 수정
- import 순서 자동 정렬

## 폴더 구조

기능/페이지 기반 구조 사용

```
src/
├── api/            # 공통 API 클라이언트
│   └── client.ts   # fetch wrapper
├── components/     # 공통 UI 컴포넌트
├── hooks/          # 공통 커스텀 훅
├── pages/          # 페이지 컴포넌트
│   └── plugin-auth/
│       ├── api/    # 페이지 전용 API
│       ├── index.ts
│       └── plugin-auth-page.tsx
├── routes/         # 라우터 설정
├── store/          # Jotai atoms
├── utils/          # 유틸리티 함수
├── types/          # 공통 타입 정의
└── assets/         # 정적 자원
```

### API 배치 규칙

- **공통 클라이언트**: `src/api/client.ts` - fetch wrapper, 인터셉터 등
- **도메인별 API**: 사용하는 페이지/기능 디렉토리 내 `api/` 폴더에 배치

```
src/
├── api/
│   └── client.ts           # 공통 fetch wrapper
└── pages/
    └── plugin-auth/
        └── api/
            └── datasource-api.ts  # plugin-auth 전용 API
```

```typescript
// ✅ 권장: 사용처에 API 배치
import { datasourceApi } from './api/datasource-api';

// ❌ 지양: 모든 API를 src/api/에 집중
import { datasourceApi } from '../../api/datasource-api';
```

## 데이터 페칭 (TanStack Query)

### 원칙

- **useEffect + useState 금지**: 서버 데이터 조회 시 `useQuery` 사용
- **useEffect 허용 케이스**: UI 상태 동기화 (예: 첫 번째 항목 자동 선택)
- **Query Factory 패턴**: API 파일에 `queries` 객체 정의, 컴포넌트에서 직접 `useQuery` 사용

### Query Factory 패턴

API와 쿼리 설정을 같은 파일에 정의하여 응집도 향상:

```typescript
// api/plugin-api.ts
export const pluginApi = {
  getPlugins: async (): Promise<Plugin[]> => {
    const response = await apiClient.get<Plugin[]>('/plugins');
    return response.data;
  },

  getFormConfig: async (pluginId: string): Promise<PluginForm> => {
    const response = await apiClient.get<PluginForm>(`/plugins/${pluginId}/form-config`);
    return response.data;
  },
};

/** Query Factory */
export const pluginQueries = {
  all: () => ({ queryKey: ['plugins'] as const }),

  list: () => ({
    queryKey: [...pluginQueries.all().queryKey, 'list'] as const,
    queryFn: () => pluginApi.getPlugins(),
  }),

  form: (pluginId: string) => ({
    queryKey: [...pluginQueries.all().queryKey, pluginId, 'form'] as const,
    queryFn: () => pluginApi.getFormConfig(pluginId),
  }),
};
```

### 컴포넌트에서 사용

```typescript
// ✅ 권장: Query Factory 사용
import { useQuery } from '@tanstack/react-query';
import { pluginQueries } from './api';

const { data: plugins = [], isLoading } = useQuery(pluginQueries.list());

// 조건부 쿼리
const { data: formData } = useQuery({
  ...pluginQueries.form(pluginId!),
  enabled: !!pluginId,
});
```

```typescript
// ❌ 지양: useEffect로 fetch
useEffect(() => {
  const fetchData = async () => {
    const data = await pluginApi.getPlugins();
    setPlugins(data);
  };
  fetchData();
}, []);
```

### Query Key 구조

```typescript
// all(): 해당 도메인 전체 무효화용
pluginQueries.all()  // ['plugins']

// list(): 목록 조회
pluginQueries.list()  // ['plugins', 'list']

// 상세 조회
pluginQueries.form(id)  // ['plugins', id, 'form']
```

### 병렬 쿼리

```typescript
form: (pluginId: string) => ({
  queryKey: [...pluginQueries.all().queryKey, pluginId, 'form'] as const,
  queryFn: async () => {
    const [formConfig, testForm] = await Promise.all([
      pluginApi.getFormConfig(pluginId),
      pluginApi.getTestForm(pluginId),
    ]);
    return { formConfig, testForm };
  },
}),
```

### 커스텀 훅 사용 시점

Query Factory로 대부분 해결. 커스텀 훅은 다음 경우에만:

- 복잡한 후처리 로직
- 여러 쿼리 조합
- 특수한 에러/로딩 처리

```typescript
// 복잡한 로직이 필요한 경우에만 커스텀 훅
export function usePluginWithPermissions(pluginId: string) {
  const { data: plugin } = useQuery(pluginQueries.detail(pluginId));
  const { data: permissions } = useQuery(permissionQueries.forPlugin(pluginId));

  const canEdit = useMemo(() => {
    // 복잡한 권한 계산 로직
  }, [plugin, permissions]);

  return { plugin, canEdit };
}
```

## 상태관리 (Jotai)

### Atom 정의 규칙

- **네이밍**: `~Atom` 접미사 사용
- **위치**: `store/` 디렉토리에 도메인별 분리
- **Immer 활용**: 복잡한 상태 업데이트 시 `produce` 사용

```typescript
// ✅ 권장
export const userAtom = atom<User | null>(null);
export const countAtom = atom(0);

// Write-only atom (Action)
export const updateUserAtom = atom(
  null,
  (get, set, newUser: User) => {
    set(userAtom, newUser);
  }
);

// ❌ 지양
export const user = atom<User | null>(null);  // 접미사 누락
```

### 파생 상태

```typescript
// Derived atom
export const userNameAtom = atom((get) => get(userAtom)?.name ?? '');
```

## 컴포넌트 규칙

### 함수 선언 스타일

- **최상위 컴포넌트**: `export function` 선언문 사용
- **내부 컴포넌트/핸들러**: arrow function 사용

```typescript
// ✅ 최상위 컴포넌트
export function UserProfile({ userId }: UserProfileProps) {
  const handleClick = () => {
    // 내부 핸들러는 arrow function
  };

  return <div>...</div>;
}

// ❌ 지양
export const UserProfile = ({ userId }: UserProfileProps) => {
  return <div>...</div>;
};
```

### Barrel Export 규칙

폴더/파일 혼동 방지를 위한 index.ts 활용

```
button/
├── index.ts        # export 진입점
├── button.tsx      # 컴포넌트 구현
├── button.module.css
└── types.ts        # Props 타입
```

```typescript
// button/index.ts
export { Button } from './button';
export type { ButtonProps } from './types';
```

```typescript
// 사용처
import { Button } from './button';  // button/index.ts 자동 참조
```

### 명시적 Export

```typescript
// ✅ 필요한 모듈만 명시적 export
export type { BlogData, BlogListResponse } from './model/types';
export { blogApi } from './api/blog-api';

// ❌ 지양: 와일드카드 export
export * from './model/types';
export * from './api/blog-api';
```

### 조건부 Barrel Export

자주 사용하는 것만 묶고, 무거운 모듈은 별도 import

```typescript
// shared/ui/index.ts
// 가벼운 기본 컴포넌트만 등록
export { Button } from './button';
export { Input } from './input';

// 무거운 컴포넌트는 직접 import
// import { Chart } from '@/shared/ui/chart';
```

## 네이밍 컨벤션

### 파일/폴더

- **kebab-case** 사용

```
components/
├── user-profile/
│   ├── index.ts
│   ├── user-profile.tsx
│   └── user-profile.module.css
└── button/
    ├── index.ts
    └── button.tsx
```

### 타입/인터페이스

- **Props**: `ComponentNameProps` 형식
- **위치**: 폴더 내 `types.ts`에 분리

```typescript
// user-profile/types.ts
export interface UserProfileProps {
  userId: string;
  onUpdate?: (user: User) => void;
}
```

### 커스텀 훅

- **패턴**: `use + 동사 + 명사`

```typescript
// ✅ 권장
export function useFetchUser(userId: string) { }
export function useGetPosts() { }
export function useSubmitForm() { }

// ❌ 지양
export function useUser() { }      // 동사 누락
export function useUserFetch() { } // 순서 불일치
```

## Import 순서

그룹별 정렬 (빈 줄로 구분)

```typescript
// 1. 외부 라이브러리
import { atom, useAtom } from 'jotai';
import { produce } from 'immer';

// 2. 내부 절대경로 모듈
import { userApi } from '@/api/user-api';
import { Button } from '@/components/button';

// 3. 상대경로 모듈
import { UserCard } from './user-card';
import type { UserProfileProps } from './types';

// 4. 스타일
import styles from './user-profile.module.css';
```

## 스타일링 (CSS Modules)

### 파일 구조

```
component-name/
├── component-name.tsx
└── component-name.module.css
```

### 사용법

```typescript
import styles from './button.module.css';
import clsx from 'clsx';

export function Button({ variant, disabled }: ButtonProps) {
  return (
    <button
      className={clsx(
        styles.button,
        styles[variant],
        disabled && styles.disabled
      )}
    >
      ...
    </button>
  );
}
```

## 에러 처리

### Query 에러 처리

```typescript
// 커스텀 훅에서 에러 문자열로 변환
export function useFetchPlugins() {
  const { data, isLoading, error } = useQuery<Plugin[]>({
    queryKey: ['plugins'],
    queryFn: () => pluginApi.getPlugins(),
  });

  return {
    plugins: data ?? [],
    isLoading,
    error: error instanceof Error ? error.message : null,
  };
}

// 컴포넌트에서 사용
const { plugins, isLoading, error } = useFetchPlugins();

if (isLoading) return <Loading />;
if (error) return <ErrorMessage message={error} />;
```

## 주석 규칙

- **최소한 주석**: 복잡한 로직에만 작성
- 코드 자체로 의도가 명확하도록 작성

```typescript
// ✅ 복잡한 비즈니스 로직 설명
// 사용자 권한이 admin이고 구독이 활성화된 경우에만 접근 허용
const hasAccess = user.role === 'admin' && subscription.isActive;

// ❌ 불필요한 주석
// 사용자 이름 가져오기
const userName = user.name;
```

---

## 타입 명세 (Backend 참조용)

> 위치: `src/store/types.ts`

### PluginForm

플러그인 폼 설정 정의. 백엔드에서 JSON 형태로 제공.

```typescript
interface PluginForm {
  pluginId: string;      // 플러그인 고유 식별자
  pluginName: string;    // 플러그인 표시명
  authType: 'oAuth2' | 'form';  // 인증 방식
  formConfig: ControlProps[];   // 폼 컨트롤 배열
}
```

### ControlProps

개별 폼 컨트롤 설정.

```typescript
type ControlType = 'INPUT_TEXT' | 'DROP_DOWN' | 'CHECKBOX' | 'RADIO_BUTTON';

interface ControlProps {
  controlType: ControlType;           // 컨트롤 유형
  label: string;                      // 라벨 텍스트
  configProperty: string;             // 값 저장 경로 (lodash path 형식)
  dataType?: 'PASSWORD' | 'TEXT';     // INPUT_TEXT 전용: 입력 타입
  initialValue?: unknown;             // 초기값
  options?: SelectOption[];           // DROP_DOWN, RADIO_BUTTON 전용
  hidden?: boolean;                   // 숨김 여부
}

interface SelectOption {
  label: string;  // 표시 텍스트
  value: string;  // 실제 값
}
```

### Datasource

데이터소스 상태 객체.

```typescript
interface Datasource {
  id: string;
  name: string;
  pluginId: string;
  datasourceConfiguration: DatasourceConfig;
}

interface DatasourceConfig {
  authenticationType?: 'oAuth2' | 'base' | 'form';
  [key: string]: unknown;  // configProperty 경로에 따라 동적 저장
}
```

### 예시: Backend JSON 응답

```json
{
  "pluginId": "s3-plugin-id",
  "pluginName": "Amazon S3",
  "authType": "form",
  "formConfig": [
    {
      "controlType": "INPUT_TEXT",
      "label": "Access Key ID",
      "configProperty": "datasourceConfiguration.authentication.username"
    },
    {
      "controlType": "INPUT_TEXT",
      "label": "Secret Access Key",
      "configProperty": "datasourceConfiguration.authentication.password",
      "dataType": "PASSWORD"
    },
    {
      "controlType": "DROP_DOWN",
      "label": "Region",
      "configProperty": "datasourceConfiguration.region",
      "options": [
        { "label": "us-east-1", "value": "us-east-1" },
        { "label": "ap-northeast-2", "value": "ap-northeast-2" }
      ]
    }
  ]
}
```
