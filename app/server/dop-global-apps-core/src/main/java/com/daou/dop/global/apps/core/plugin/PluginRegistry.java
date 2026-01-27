package com.daou.dop.global.apps.core.plugin;

import com.daou.dop.global.apps.core.dto.OAuthTokenInfo;
import com.daou.dop.global.apps.core.dto.PluginConfigInfo;
import com.daou.dop.global.apps.core.oauth.OAuthException;
import com.daou.dop.global.apps.core.oauth.PluginOAuthService;
import com.daou.dop.global.apps.plugin.sdk.OAuthHandler;
import com.daou.dop.global.apps.plugin.sdk.PluginConfig;
import com.daou.dop.global.apps.plugin.sdk.PluginExecutor;
import com.daou.dop.global.apps.plugin.sdk.TokenInfo;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 플러그인 레지스트리
 * OAuthHandler, PluginExecutor 등 플러그인 확장점 조회 및 래핑
 */
@Component
public class PluginRegistry implements PluginOAuthService {

    private static final Logger log = LoggerFactory.getLogger(PluginRegistry.class);

    private final PluginManager pluginManager;

    public PluginRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    // ========== PluginOAuthService 구현 ==========

    @Override
    public boolean supportsOAuth(String pluginId) {
        return findOAuthHandler(pluginId).isPresent();
    }

    @Override
    public String buildAuthorizationUrl(String pluginId, PluginConfigInfo config, String state, String redirectUri) {
        OAuthHandler handler = findOAuthHandler(pluginId)
                .orElseThrow(() -> new OAuthException("OAuthHandler not found: " + pluginId));

        PluginConfig sdkConfig = toPluginConfig(config);
        return handler.buildAuthorizationUrl(sdkConfig, state, redirectUri);
    }

    @Override
    public OAuthTokenInfo exchangeCode(String pluginId, PluginConfigInfo config, String code, String redirectUri)
            throws OAuthException {
        OAuthHandler handler = findOAuthHandler(pluginId)
                .orElseThrow(() -> new OAuthException("OAuthHandler not found: " + pluginId));

        try {
            PluginConfig sdkConfig = toPluginConfig(config);
            TokenInfo tokenInfo = handler.exchangeCode(sdkConfig, code, redirectUri);
            return toOAuthTokenInfo(tokenInfo);
        } catch (com.daou.dop.global.apps.plugin.sdk.OAuthException e) {
            throw new OAuthException(e.getMessage(), e);
        }
    }

    @Override
    public boolean requiresPkce(String pluginId) {
        return findOAuthHandler(pluginId)
                .map(OAuthHandler::requiresPkce)
                .orElse(false);
    }

    // ========== 플러그인 리소스 조회 ==========

    /**
     * 플러그인 JAR 내 리소스 스트림 조회
     * 개발 환경에서 ClassLoader 공유 문제 해결을 위해 pluginId 기반 경로 우선 사용
     */
    public Optional<InputStream> getPluginResourceStream(String pluginId, String resourceName) {
        Optional<PluginExecutor> pluginExecutor = findPluginExecutor(pluginId);
        return pluginExecutor.map(executor -> {
            ClassLoader classLoader = executor.getClass().getClassLoader();
            // 1. pluginId 기반 경로 시도 (예: google-calendar/form-config.json)
            InputStream stream = classLoader.getResourceAsStream(pluginId + "/" + resourceName);
            if (stream != null) {
                log.debug("Loaded resource from pluginId path: {}/{}", pluginId, resourceName);
                return stream;
            }
            // 2. 기존 경로 fallback (예: form-config.json)
            log.debug("Fallback to root path: {}", resourceName);
            return classLoader.getResourceAsStream(resourceName);
        });
    }

    // ========== 내부 조회 메서드 ==========

    private Optional<OAuthHandler> findOAuthHandler(String pluginId) {
        List<OAuthHandler> handlers = pluginManager.getExtensions(OAuthHandler.class);
        return handlers.stream()
                .filter(handler -> handler.getPluginId().equalsIgnoreCase(pluginId))
                .findFirst();
    }

    Optional<PluginExecutor> findPluginExecutor(String pluginId) {
        List<PluginExecutor> executors = pluginManager.getExtensions(PluginExecutor.class);
        return executors.stream()
                .filter(executor -> executor.getPluginId().equalsIgnoreCase(pluginId))
                .findFirst();
    }

    /**
     * 지원하는 플러그인 ID 목록
     */
    public List<String> getSupportedPluginIds() {
        return pluginManager.getExtensions(OAuthHandler.class).stream()
                .map(OAuthHandler::getPluginId)
                .toList();
    }

    // ========== 변환 메서드 ==========

    private PluginConfig toPluginConfig(PluginConfigInfo info) {
        return PluginConfig.builder()
                .pluginId(info.pluginId())
                .clientId(info.clientId())
                .clientSecret(info.clientSecret())
                .secrets(info.secrets())
                .metadata(info.metadata())
                .build();
    }

    private OAuthTokenInfo toOAuthTokenInfo(TokenInfo tokenInfo) {
        return OAuthTokenInfo.builder()
                .pluginId(tokenInfo.pluginId())
                .externalId(tokenInfo.externalId())
                .externalName(tokenInfo.externalName())
                .accessToken(tokenInfo.accessToken())
                .refreshToken(tokenInfo.refreshToken())
                .scope(tokenInfo.scope())
                .expiresAt(tokenInfo.expiresAt())
                .installedAt(tokenInfo.installedAt())
                .metadata(tokenInfo.metadata())
                .build();
    }
}
