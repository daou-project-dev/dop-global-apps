# CLAUDE.md

이 파일은 Claude Code가 프로젝트 작업 시 참고하는 설정 파일입니다.

** 모든 문서와 답변은 간결하게 개조체 한국어로 작성합니다.**

## 답변 스타일

- **개조식**: 간결하게 핵심만 전달
- **금지 표현**: "~할게", "~했어", "~함" 등 구어체
- **권장 표현**: "~ 추가", "~ 완료", "~ 필요" 등 명사형 종결

## 다이어그램 작성 규칙

- **대화 중 설명**: ASCII art 사용 (가독성 우선)
- **문서 기록 시**: Mermaid 형식으로 작성
- **코드 블록**: 언어 명시 필수 (```mermaid, ```typescript 등)

## 모듈별 가이드

- **Frontend**: [app/client/CLAUDE.md](app/client/CLAUDE.md)
- **Backend**: [app/server/CLAUDE.md](app/server/CLAUDE.md)

## 백엔드 의존성 구조

```
                      api
                       │
       ┌───────────────┼───────────────┐
       │ impl          │               │ runtimeOnly
       ▼               │               ▼
     core ◀────────────┼───────── infrastructure
       │            impl               │
       ├─impl─▶ plugin-sdk             │ impl
       │       (전이 불가)              │
       │                               │
       └─impl──────▶ domain ◀──────────┘
                    (전이 불가)
```

- `infrastructure → core`: Repository 인터페이스 구현 (DIP)

### 모듈별 역할

| 모듈 | 역할 | 포함 내용 |
|------|------|----------|
| **api** | HTTP 진입점 | Controller만, core DTO 사용 |
| **core** | 비즈니스 로직 | DTO, Service, Repository Port, 타입 변환 |
| **domain** | 도메인 모델 | Entity, Enum |
| **infrastructure** | 기술 구현체 | JPA Repository, Crypto, Config |
| **plugin-sdk** | 플러그인 SDK | PluginExecutor, OAuthHandler, SDK DTO |

### 핵심 원칙

- api는 **core DTO만** 사용 (domain Entity, plugin-sdk 타입 직접 접근 불가)
- 모든 타입 변환은 **core에서** 수행
- `implementation` 의존 = 전이 불가 (타입 격리)

> 상세 설계: [app/server/docs/DESIGN/BACKEND_LAYER.md](app/server/docs/DESIGN/BACKEND_LAYER.md)