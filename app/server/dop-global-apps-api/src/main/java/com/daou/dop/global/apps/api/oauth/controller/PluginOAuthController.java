package com.daou.dop.global.apps.api.oauth.controller;

import com.daou.dop.global.apps.core.connection.ConnectionService;
import com.daou.dop.global.apps.core.dto.OAuthTokenInfo;
import com.daou.dop.global.apps.core.dto.PluginConfigInfo;
import com.daou.dop.global.apps.core.oauth.OAuthException;
import com.daou.dop.global.apps.core.oauth.PluginOAuthService;
import com.daou.dop.global.apps.core.oauth.StateStorage;
import com.daou.dop.global.apps.core.plugin.PluginService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * 범용 OAuth 컨트롤러
 * 모든 플러그인의 OAuth 요청 처리
 */
@RestController
@RequestMapping("/oauth")
public class PluginOAuthController {

    private static final Logger log = LoggerFactory.getLogger(PluginOAuthController.class);
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final PluginOAuthService pluginOAuthService;
    private final PluginService pluginService;
    private final ConnectionService connectionService;
    private final StateStorage stateStorage;

    public PluginOAuthController(
            PluginOAuthService pluginOAuthService,
            PluginService pluginService,
            ConnectionService connectionService,
            StateStorage stateStorage) {
        this.pluginOAuthService = pluginOAuthService;
        this.pluginService = pluginService;
        this.connectionService = connectionService;
        this.stateStorage = stateStorage;
    }

    /**
     * OAuth 설치 시작
     * GET /oauth/{plugin}/install
     */
    @GetMapping("/{plugin}/install")
    public ResponseEntity<String> startInstall(
            @PathVariable("plugin") String pluginId,
            HttpServletRequest request) {

        if (!pluginOAuthService.supportsOAuth(pluginId)) {
            log.warn("OAuthHandler not found for plugin: {}", pluginId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Unknown plugin: " + pluginId);
        }

        PluginConfigInfo config = pluginService.getPluginConfig(pluginId).orElse(null);
        if (config == null) {
            log.warn("Plugin config not found in DB: {}", pluginId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Plugin not configured: " + pluginId);
        }

        String state = stateStorage.generateAndStore(pluginId, STATE_TTL);
        String redirectUri = buildRedirectUri(request, pluginId);
        String authorizationUrl = pluginOAuthService.buildAuthorizationUrl(pluginId, config, state, redirectUri);

        log.info("Starting OAuth for plugin: {}", pluginId);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", authorizationUrl)
                .build();
    }

    /**
     * OAuth 콜백
     * GET /oauth/{plugin}/callback
     */
    @GetMapping("/{plugin}/callback")
    public ResponseEntity<String> handleCallback(
            @PathVariable("plugin") String pluginId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request) {

        if (error != null) {
            log.warn("OAuth error for plugin {}: {}", pluginId, error);
            return ResponseEntity.badRequest()
                    .body("Installation failed: " + error);
        }

        if (!pluginOAuthService.supportsOAuth(pluginId)) {
            log.warn("OAuthHandler not found for plugin: {}", pluginId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Unknown plugin: " + pluginId);
        }

        PluginConfigInfo config = pluginService.getPluginConfig(pluginId).orElse(null);
        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Plugin not configured: " + pluginId);
        }

        if (!stateStorage.validateAndConsume(pluginId, state)) {
            log.warn("Invalid state for plugin {}: {}", pluginId, state);
            return ResponseEntity.badRequest()
                    .body("Invalid state. Please try again.");
        }

        try {
            String redirectUri = buildRedirectUri(request, pluginId);
            OAuthTokenInfo tokenInfo = pluginOAuthService.exchangeCode(pluginId, config, code, redirectUri);

            Long connectionId = connectionService.saveOAuthToken(tokenInfo);

            log.info("OAuth successful for plugin {}: {} ({}) - connectionId={}",
                    pluginId, tokenInfo.externalName(), tokenInfo.externalId(), connectionId);

            return ResponseEntity.ok("Installation successful! You can close this window.");
        } catch (OAuthException e) {
            log.error("OAuth failed for plugin {}: {}", pluginId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Installation failed: " + e.getMessage());
        }
    }

    private String buildRedirectUri(HttpServletRequest request, String pluginId) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder uri = new StringBuilder();
        uri.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && serverPort != 80) ||
                ("https".equals(scheme) && serverPort != 443)) {
            uri.append(":").append(serverPort);
        }

        uri.append(contextPath);
        uri.append("/oauth/").append(pluginId).append("/callback");

        return uri.toString();
    }
}
