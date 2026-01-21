package com.daou.dop.global.apps.server.plugin;

import com.daou.dop.global.apps.core.oauth.OAuthHandler;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 플러그인 레지스트리
 * OAuthHandler 등 플러그인 확장점 조회
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
     * 지원하는 플러그인 ID 목록
     */
    public List<String> getSupportedPluginIds() {
        return getAllOAuthHandlers().stream()
                .map(OAuthHandler::getPluginId)
                .toList();
    }
}
