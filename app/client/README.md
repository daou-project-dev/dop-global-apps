# DOP Global Apps - Frontend

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | React | 19.x |
| Language | TypeScript | 5.9.x |
| Build Tool | Vite | 7.x |
| Router | TanStack Router | 1.x |
| 상태관리 | Jotai | 2.x |
| 불변성 | Immer | 11.x |
| 스타일링 | CSS Modules | - |

## 사전 요구사항

- Node.js 20.x 이상
- npm 또는 yarn
- 백엔드 서버 실행 (포트 8443)

## 설치

```bash
# 프로젝트 클론 후 client 디렉토리로 이동
cd app/client

# 의존성 설치
npm install
```

## 환경변수 설정

### 환경변수 파일 구조

```
app/client/
├── .env.development     # 개발 환경 (git 포함)
├── .env.production      # 프로덕션 환경 (git 포함)
└── .env.local           # 로컬 오버라이드 (git 제외)
```

### 환경변수 명세

| 변수명 | 설명 | 필수 | 예시 |
|--------|------|------|------|
| `VITE_API_BASE_URL` | API 서버 기본 URL | O | `https://localhost:8443` |

### 환경별 기본값

**개발 환경** (`.env.development`)
```env
VITE_API_BASE_URL=https://localhost:8443
```

**프로덕션 환경** (`.env.production`)
```env
VITE_API_BASE_URL=https://api.example.com
```

### 로컬 환경 설정

개인 설정이 필요한 경우 `.env.local` 파일 생성:

```bash
# .env.local 생성 (git에서 제외됨)
cp .env.development .env.local
```

```env
# .env.local
VITE_API_BASE_URL=https://your-custom-api.com
```

> **참고**: `.env.local`은 다른 환경 파일보다 우선 적용됨

## SSL 인증서 설정

개발 환경에서 HTTPS 사용을 위해 mkcert 인증서 필요:

```bash
# mkcert 설치 (macOS)
brew install mkcert
mkcert -install

# 프로젝트 루트의 certs 디렉토리에 인증서 생성
cd ../../certs  # app/client에서 프로젝트 루트/certs로 이동
mkcert localhost
```

인증서 파일 구조:
```
certs/
├── localhost.pem      # 인증서
└── localhost-key.pem  # 개인키
```

## 실행

```bash
# 개발 서버 실행
npm run dev
```

개발 서버 실행 시:
- URL: `https://localhost:5173`
- API 프록시: `/api` → `https://localhost:8443`

## 빌드

```bash
# 프로덕션 빌드
npm run build

# 빌드 결과 미리보기
npm run preview
```

## 스크립트

| 명령어 | 설명 |
|--------|------|
| `npm run dev` | 개발 서버 실행 |
| `npm run build` | 타입 체크 + 프로덕션 빌드 |
| `npm run preview` | 빌드 결과 미리보기 |
| `npm run lint` | ESLint 검사 |

## 프로젝트 구조

```
src/
├── api/                # 공통 API 클라이언트
│   └── client.ts       # fetch wrapper
├── components/         # 공통 UI 컴포넌트
│   ├── controls/       # 폼 입력 컨트롤
│   ├── form-renderer/  # 동적 폼 렌더링
│   ├── gnb/            # 상단 네비게이션
│   └── layout/         # 레이아웃
├── pages/              # 페이지 컴포넌트
│   ├── plugin-auth/    # 플러그인 인증
│   │   ├── api/        # 페이지 전용 API
│   │   ├── components/ # 페이지 하위 컴포넌트
│   │   └── hooks/      # 페이지 커스텀 훅
│   └── datasources/    # 데이터소스 관리
├── routes/             # TanStack Router 설정
├── store/              # Jotai 상태관리
└── assets/             # 정적 자원
```

## 라우팅

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/` | PluginAuthPage | 플러그인 인증 설정 |
| `/datasources` | DatasourcesPage | 데이터소스 목록 |

## 트러블슈팅

### 인증서 오류

```
Error: UNABLE_TO_GET_ISSUER_CERT_LOCALLY
```

해결: mkcert 설치 후 인증서 재생성

```bash
mkcert -install
cd certs && mkcert localhost
```

### API 연결 실패

1. 백엔드 서버 실행 상태 확인 (포트 8443)
2. `.env.local`의 `VITE_API_BASE_URL` 확인
3. 브라우저 개발자 도구 Network 탭에서 요청 URL 확인

### 포트 충돌

```bash
# 5173 포트 사용 중인 프로세스 확인
lsof -i :5173

# 다른 포트로 실행
npm run dev -- --port 3000
```

## 참고 문서

- 개발 가이드: [CLAUDE.md](./CLAUDE.md)
- Vite 문서: https://vite.dev
- TanStack Router: https://tanstack.com/router
- Jotai: https://jotai.org
