package com.daou.dop.global.apps.api.plugin;

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
 */
@Component
public class PluginRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginRegistry.class);

    private final PluginManager pluginManager;

    public PluginRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * 플러그인 ID로 OAuthHandler 조회
     */
    public Optional<OAuthHandler> findOAuthHandler(String pluginId) {
        List<OAuthHandler> handlers = pluginManager.getExtensions(OAuthHandler.class);

        return handlers.stream()
                .filter(handler -> handler.getPluginId().equalsIgnoreCase(pluginId))
                .findFirst();
    }

    /**
     * 모든 OAuthHandler 조회
     */
    public List<OAuthHandler> getAllOAuthHandlers() {
        return pluginManager.getExtensions(OAuthHandler.class);
    }

    /**
     * 플러그인 ID로 PluginExecutor 조회
     */
    public Optional<PluginExecutor> findPluginExecutor(String pluginId) {
        List<PluginExecutor> executors = pluginManager.getExtensions(PluginExecutor.class);

        return executors.stream()
                .filter(executor -> executor.getPluginId().equalsIgnoreCase(pluginId))
                .findFirst();
    }

    /**
     * 모든 PluginExecutor 조회
     */
    public List<PluginExecutor> getAllPluginExecutors() {
        return pluginManager.getExtensions(PluginExecutor.class);
    }

    /**
     * 지원하는 플러그인 ID 목록
     */
    public List<String> getSupportedPluginIds() {
        return getAllOAuthHandlers().stream()
                .map(OAuthHandler::getPluginId)
                .toList();
    }
}
