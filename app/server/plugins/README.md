# 플러그인 개발 가이드

## 디렉토리 구조

```
plugins/
├── plugin-sdk/                 # SDK 인터페이스 (의존성으로 사용)
├── slack-plugin/               # 참고 구현체
│   ├── build.gradle
│   └── src/main/
│       ├── java/.../slack/
│       │   ├── SlackPlugin.java           # Plugin 클래스
│       │   ├── SlackPluginExecutor.java   # @Extension 구현
│       │   └── SlackOAuthHandler.java     # @Extension (OAuth 지원 시)
│       └── resources/
│           └── slack/                     # ⚠️ pluginId와 동일한 폴더명
│               ├── form-config.json
│               └── test-form.json
└── {new-plugin}/               # 신규 플러그인
```

---

## 리소스 경로 규칙

### ⚠️ 중요: pluginId 폴더 필수

리소스 파일은 반드시 **pluginId와 동일한 폴더** 하위에 위치해야 함.

```
src/main/resources/
└── {pluginId}/              ← getPluginId() 반환값과 일치
    ├── form-config.json     # 인증/설정 폼
    └── test-form.json       # API 테스트 폼
```

**예시:**

| pluginId | 리소스 경로 |
|----------|-------------|
| `slack` | `resources/slack/form-config.json` |
| `google-calendar` | `resources/google-calendar/form-config.json` |

### 이유

로컬 개발 환경에서 플러그인들이 동일한 classpath를 공유하기 때문에, 리소스 파일명만으로는 구분 불가.
pluginId 폴더로 네임스페이스를 분리하여 충돌 방지.

---

## 필수 구현 체크리스트

### 1. Plugin 클래스

```java
public class MyPlugin extends Plugin {
    public MyPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
}
```

### 2. PluginExecutor 구현 (필수)

```java
@Extension
public class MyPluginExecutor implements PluginExecutor {

    private static final String PLUGIN_ID = "my-plugin";  // ⚠️ 리소스 폴더명과 일치

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public List<String> getSupportedActions() {
        return List.of("action1", "action2");
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        // 액션 처리 로직
    }
}
```

### 3. OAuthHandler 구현 (OAuth 지원 시)

```java
@Extension
public class MyOAuthHandler implements OAuthHandler {

    private static final String PLUGIN_ID = "my-plugin";  // ⚠️ 동일한 ID

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public String buildAuthorizationUrl(PluginConfig config, String state, String redirectUri) {
        // OAuth 인증 URL 생성
    }

    @Override
    public TokenInfo exchangeCode(PluginConfig config, String code, String redirectUri) {
        // 인증 코드 → 토큰 교환
    }
}
```

---

## build.gradle 설정

```groovy
plugins {
    id 'java'
}

dependencies {
    // plugin-sdk 의존 (필수)
    compileOnly project(':plugins:plugin-sdk')

    // PF4J 어노테이션 프로세서 (필수)
    annotationProcessor(libs.pf4j)

    // 플러그인별 추가 의존성
    // implementation 'com.example:some-lib:1.0.0'
}

jar {
    manifest {
        attributes 'Plugin-Class': 'com.daou.dop.global.apps.plugin.myplugin.MyPlugin',
                   'Plugin-Id': 'my-plugin',      // ⚠️ PLUGIN_ID와 일치
                   'Plugin-Version': '0.0.1',
                   'Plugin-Provider': 'Daou Tech'
    }
}
```

---

## 빌드 및 테스트

```bash
# 전체 빌드
./gradlew build

# 특정 플러그인만 빌드
./gradlew :plugins:my-plugin:build

# 서버 실행 (로컬)
./gradlew bootRun

# API 테스트
curl http://localhost:8080/api/plugins/my-plugin/form-config
```

---

## 신규 플러그인 생성 순서

1. `plugins/` 하위에 디렉토리 생성
2. `build.gradle` 작성 (위 템플릿 참고)
3. `settings.gradle`에 모듈 추가
   ```groovy
   include ':plugins:my-plugin'
   ```
4. `dop-global-apps-api/build.gradle`에 의존성 추가
   ```groovy
   runtimeOnly project(':plugins:my-plugin')
   ```
5. Plugin, PluginExecutor 클래스 구현
6. `resources/{pluginId}/` 폴더에 JSON 리소스 배치
7. 빌드 및 테스트

---

## 관련 문서

- [플러그인 아키텍처 설계](../docs/DESIGN/PLUGIN.md) - DTO, 인터페이스 상세 설계
- [plugin-sdk](./plugin-sdk/) - SDK 인터페이스 소스
