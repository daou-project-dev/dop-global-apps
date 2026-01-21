package com.daou.dop.global.apps.server.oauth.controller;

import com.daou.dop.global.apps.core.oauth.OAuthHandler;
import com.daou.dop.global.apps.core.oauth.StateStorage;
import com.daou.dop.global.apps.core.oauth.TokenInfo;
import com.daou.dop.global.apps.core.oauth.TokenStorage;
import com.daou.dop.global.apps.server.plugin.PluginRegistry;
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

    private final PluginRegistry pluginRegistry;
    private final StateStorage stateStorage;
    private final TokenStorage tokenStorage;

    public PluginOAuthController(
            PluginRegistry pluginRegistry,
            StateStorage stateStorage,
            TokenStorage tokenStorage) {
        this.pluginRegistry = pluginRegistry;
        this.stateStorage = stateStorage;
        this.tokenStorage = tokenStorage;
    }

    /**
     * OAuth 설치 시작
     * GET /oauth/{plugin}/install
     */
    @GetMapping("/{plugin}/install")
    public ResponseEntity<String> startInstall(
            @PathVariable("plugin") String pluginId,
            HttpServletRequest request) {

        OAuthHandler handler = pluginRegistry.findOAuthHandler(pluginId)
                .orElse(null);

        if (handler == null) {
            log.warn("Unknown plugin: {}", pluginId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Unknown plugin: " + pluginId);
        }

        String state = stateStorage.generateAndStore(pluginId, STATE_TTL);
        String redirectUri = buildRedirectUri(request, pluginId);
        String authorizationUrl = handler.buildAuthorizationUrl(state, redirectUri);

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

        // 에러 응답 처리
        if (error != null) {
            log.warn("OAuth error for plugin {}: {}", pluginId, error);
            return ResponseEntity.badRequest()
                    .body("Installation failed: " + error);
        }

        // 플러그인 확인
        OAuthHandler handler = pluginRegistry.findOAuthHandler(pluginId)
                .orElse(null);

        if (handler == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Unknown plugin: " + pluginId);
        }

        // State 검증 (CSRF 방지)
        if (!stateStorage.validateAndConsume(pluginId, state)) {
            log.warn("Invalid state for plugin {}: {}", pluginId, state);
            return ResponseEntity.badRequest()
                    .body("Invalid state. Please try again.");
        }

        try {
            String redirectUri = buildRedirectUri(request, pluginId);
            TokenInfo tokenInfo = handler.exchangeCode(code, redirectUri);
            tokenStorage.save(tokenInfo);

            log.info("OAuth successful for plugin {}: {} ({})",
                    pluginId, tokenInfo.externalName(), tokenInfo.externalId());

            return ResponseEntity.ok("Installation successful! You can close this window.");
        } catch (Exception e) {
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
