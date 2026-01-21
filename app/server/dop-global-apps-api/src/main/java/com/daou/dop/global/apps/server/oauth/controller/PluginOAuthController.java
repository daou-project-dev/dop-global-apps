package com.daou.dop.global.apps.server.oauth.controller;

import com.daou.dop.global.apps.core.oauth.StateStorage;
import com.daou.dop.global.apps.core.oauth.TokenStorage;
import com.daou.dop.global.apps.plugin.sdk.OAuthHandler;
import com.daou.dop.global.apps.plugin.sdk.PluginConfig;
import com.daou.dop.global.apps.plugin.sdk.TokenInfo;
import com.daou.dop.global.apps.domain.connection.PluginConnection;
import com.daou.dop.global.apps.server.connection.service.ConnectionService;
import com.daou.dop.global.apps.server.plugin.PluginRegistry;
import com.daou.dop.global.apps.server.plugin.service.PluginService;
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
 *
 * <p>V2: PluginConfig를 DB에서 조회하여 플러그인에 전달
 * <p>V2: ConnectionService로 연동/토큰 저장
 */
@RestController
@RequestMapping("/oauth")
public class PluginOAuthController {

    private static final Logger log = LoggerFactory.getLogger(PluginOAuthController.class);
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final PluginRegistry pluginRegistry;
    private final PluginService pluginService;
    private final ConnectionService connectionService;
    private final StateStorage stateStorage;
    private final TokenStorage tokenStorage;  // V1 폴백용

    public PluginOAuthController(
            PluginRegistry pluginRegistry,
            PluginService pluginService,
            ConnectionService connectionService,
            StateStorage stateStorage,
            TokenStorage tokenStorage) {
        this.pluginRegistry = pluginRegistry;
        this.pluginService = pluginService;
        this.connectionService = connectionService;
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

        // V2 핸들러 조회
        OAuthHandler handler = pluginRegistry.findOAuthHandler(pluginId).orElse(null);
        if (handler == null) {
            // V1 폴백
            return startInstallV1(pluginId, request);
        }

        // DB에서 PluginConfig 조회
        PluginConfig config = pluginService.getPluginConfig(pluginId).orElse(null);
        if (config == null) {
            log.warn("Plugin config not found in DB: {}", pluginId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Plugin not configured: " + pluginId);
        }

        String state = stateStorage.generateAndStore(pluginId, STATE_TTL);
        String redirectUri = buildRedirectUri(request, pluginId);
        String authorizationUrl = handler.buildAuthorizationUrl(config, state, redirectUri);

        log.info("Starting OAuth V2 for plugin: {}", pluginId);

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

        // V2 핸들러 조회
        OAuthHandler handler = pluginRegistry.findOAuthHandler(pluginId).orElse(null);
        if (handler == null) {
            // V1 폴백
            return handleCallbackV1(pluginId, code, state, request);
        }

        // DB에서 PluginConfig 조회
        PluginConfig config = pluginService.getPluginConfig(pluginId).orElse(null);
        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Plugin not configured: " + pluginId);
        }

        // State 검증 (CSRF 방지)
        if (!stateStorage.validateAndConsume(pluginId, state)) {
            log.warn("Invalid state for plugin {}: {}", pluginId, state);
            return ResponseEntity.badRequest()
                    .body("Invalid state. Please try again.");
        }

        try {
            String redirectUri = buildRedirectUri(request, pluginId);
            TokenInfo tokenInfo = handler.exchangeCode(config, code, redirectUri);

            // V2: ConnectionService로 연동/토큰 저장
            PluginConnection connection = connectionService.saveOAuthToken(tokenInfo);

            log.info("OAuth V2 successful for plugin {}: {} ({}) - connectionId={}",
                    pluginId, tokenInfo.externalName(), tokenInfo.externalId(), connection.getId());

            return ResponseEntity.ok("Installation successful! You can close this window.");
        } catch (Exception e) {
            log.error("OAuth V2 failed for plugin {}: {}", pluginId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Installation failed: " + e.getMessage());
        }
    }

    // ========== V1 폴백 (Legacy) ==========

    private ResponseEntity<String> startInstallV1(String pluginId, HttpServletRequest request) {
        var handler = pluginRegistry.findOAuthHandlerV1(pluginId).orElse(null);
        if (handler == null) {
            log.warn("Unknown plugin: {}", pluginId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Unknown plugin: " + pluginId);
        }

        String state = stateStorage.generateAndStore(pluginId, STATE_TTL);
        String redirectUri = buildRedirectUri(request, pluginId);
        String authorizationUrl = handler.buildAuthorizationUrl(state, redirectUri);

        log.info("Starting OAuth V1 (legacy) for plugin: {}", pluginId);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", authorizationUrl)
                .build();
    }

    private ResponseEntity<String> handleCallbackV1(String pluginId, String code, String state, HttpServletRequest request) {
        var handler = pluginRegistry.findOAuthHandlerV1(pluginId).orElse(null);
        if (handler == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Unknown plugin: " + pluginId);
        }

        if (!stateStorage.validateAndConsume(pluginId, state)) {
            log.warn("Invalid state for plugin {}: {}", pluginId, state);
            return ResponseEntity.badRequest()
                    .body("Invalid state. Please try again.");
        }

        try {
            String redirectUri = buildRedirectUri(request, pluginId);
            var tokenInfo = handler.exchangeCode(code, redirectUri);
            tokenStorage.save(tokenInfo);

            log.info("OAuth V1 (legacy) successful for plugin {}: {} ({})",
                    pluginId, tokenInfo.externalName(), tokenInfo.externalId());

            return ResponseEntity.ok("Installation successful! You can close this window.");
        } catch (Exception e) {
            log.error("OAuth V1 failed for plugin {}: {}", pluginId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Installation failed: " + e.getMessage());
        }
    }

    // ========== Helper ==========

    /**
     * V2 TokenInfo → V1 TokenInfo 변환
     */
    private com.daou.dop.global.apps.core.oauth.TokenInfo toV1TokenInfo(TokenInfo v2) {
        return com.daou.dop.global.apps.core.oauth.TokenInfo.builder()
                .pluginId(v2.pluginId())
                .externalId(v2.externalId())
                .externalName(v2.externalName())
                .accessToken(v2.accessToken())
                .refreshToken(v2.refreshToken())
                .scope(v2.scope())
                .expiresAt(v2.expiresAt())
                .installedAt(v2.installedAt())
                .metadata(v2.metadata())
                .build();
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
