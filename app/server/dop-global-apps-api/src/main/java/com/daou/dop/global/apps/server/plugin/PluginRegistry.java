package com.daou.dop.global.apps.server.plugin;

import com.daou.dop.global.apps.plugin.sdk.OAuthHandler;
import com.daou.dop.global.apps.plugin.sdk.PluginExecutor;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 플러그인 레지스트리
 * OAuthHandler, PluginExecutor 등 플러그인 확장점 조회
 *
 * <p>V2 인터페이스(plugin-sdk) 우선 사용
 */
@Component
public class PluginRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginRegistry.class);

    private final PluginManager pluginManager;

    public PluginRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    // ========== OAuthHandler (V2) ==========

    /**
     * 플러그인 ID로 OAuthHandler V2 조회
     */
    public Optional<OAuthHandler> findOAuthHandler(String pluginId) {
        List<OAuthHandler> handlers = pluginManager.getExtensions(OAuthHandler.class);

        return handlers.stream()
                .filter(handler -> handler.getPluginId().equalsIgnoreCase(pluginId))
                .findFirst();
    }

    /**
     * 모든 OAuthHandler V2 조회
     */
    public List<OAuthHandler> getAllOAuthHandlers() {
        return pluginManager.getExtensions(OAuthHandler.class);
    }

    // ========== OAuthHandler (V1 - Legacy) ==========

    /**
     * 플러그인 ID로 OAuthHandler V1 조회 (Legacy)
     */
    public Optional<com.daou.dop.global.apps.core.oauth.OAuthHandler> findOAuthHandlerV1(String pluginId) {
        List<com.daou.dop.global.apps.core.oauth.OAuthHandler> handlers =
                pluginManager.getExtensions(com.daou.dop.global.apps.core.oauth.OAuthHandler.class);

        return handlers.stream()
                .filter(handler -> handler.getPluginId().equalsIgnoreCase(pluginId))
                .findFirst();
    }

    // ========== PluginExecutor (V2) ==========

    /**
     * 플러그인 ID로 PluginExecutor V2 조회
     */
    public Optional<PluginExecutor> findPluginExecutor(String pluginId) {
        List<PluginExecutor> executors = pluginManager.getExtensions(PluginExecutor.class);

        return executors.stream()
                .filter(executor -> executor.getPluginId().equalsIgnoreCase(pluginId))
                .findFirst();
    }

    /**
     * 모든 PluginExecutor V2 조회
     */
    public List<PluginExecutor> getAllPluginExecutors() {
        return pluginManager.getExtensions(PluginExecutor.class);
    }

    // ========== PluginExecutor (V1 - Legacy) ==========

    /**
     * 플러그인 이름으로 PluginExecutor V1 조회 (Legacy)
     */
    public Optional<com.daou.dop.global.apps.core.execute.PluginExecutor> findPluginExecutorV1(String pluginName) {
        List<com.daou.dop.global.apps.core.execute.PluginExecutor> executors =
                pluginManager.getExtensions(com.daou.dop.global.apps.core.execute.PluginExecutor.class);

        return executors.stream()
                .filter(executor -> executor.getPluginName().equalsIgnoreCase(pluginName))
                .findFirst();
    }

    // ========== Common ==========

    /**
     * 지원하는 플러그인 ID 목록 (V2 기준)
     */
    public List<String> getSupportedPluginIds() {
        return getAllOAuthHandlers().stream()
                .map(OAuthHandler::getPluginId)
                .toList();
    }
}
