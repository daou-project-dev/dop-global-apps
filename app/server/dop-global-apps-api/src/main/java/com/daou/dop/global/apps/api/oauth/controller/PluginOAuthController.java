package com.daou.dop.global.apps.api.oauth.controller;

import com.daou.dop.global.apps.core.connection.ConnectionService;
import com.daou.dop.global.apps.core.dto.OAuthTokenInfo;
import com.daou.dop.global.apps.core.dto.PluginConfigInfo;
import com.daou.dop.global.apps.core.oauth.OAuthException;
import com.daou.dop.global.apps.core.oauth.PkceStorage;
import com.daou.dop.global.apps.core.oauth.PluginOAuthService;
import com.daou.dop.global.apps.core.oauth.StateStorage;
import com.daou.dop.global.apps.core.plugin.PluginService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 범용 OAuth 컨트롤러
 * 모든 플러그인의 OAuth 요청 처리
 */
@RestController
@RequestMapping("/oauth")
public class PluginOAuthController {

    private static final Logger log = LoggerFactory.getLogger(PluginOAuthController.class);
    private static final Duration STATE_TTL = Duration.ofMinutes(10);
    private static final Set<String> PKCE_REQUIRED_PLUGINS = Set.of("ms365-calendar");

    private final PluginOAuthService pluginOAuthService;
    private final PluginService pluginService;
    private final ConnectionService connectionService;
    private final StateStorage stateStorage;
    private final PkceStorage pkceStorage;

    public PluginOAuthController(
            PluginOAuthService pluginOAuthService,
            PluginService pluginService,
            ConnectionService connectionService,
            StateStorage stateStorage,
            PkceStorage pkceStorage) {
        this.pluginOAuthService = pluginOAuthService;
        this.pluginService = pluginService;
        this.connectionService = connectionService;
        this.stateStorage = stateStorage;
        this.pkceStorage = pkceStorage;
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

        // PKCE 지원 플러그인의 경우 code_challenge 추가
        PluginConfigInfo configWithPkce = config;
        if (PKCE_REQUIRED_PLUGINS.contains(pluginId)) {
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            pkceStorage.store(state, codeVerifier, STATE_TTL);

            Map<String, Object> metadata = new HashMap<>(config.metadata() != null ? config.metadata() : Map.of());
            metadata.put("code_challenge", codeChallenge);
            metadata.put("code_challenge_method", "S256");

            configWithPkce = PluginConfigInfo.builder()
                    .pluginId(config.pluginId())
                    .displayName(config.displayName())
                    .clientId(config.clientId())
                    .clientSecret(config.clientSecret())
                    .secrets(config.secrets())
                    .metadata(metadata)
                    .build();

            log.debug("PKCE enabled for plugin: {}", pluginId);
        }

        String authorizationUrl = pluginOAuthService.buildAuthorizationUrl(pluginId, configWithPkce, state, redirectUri);

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

        // PKCE code_verifier 조회 (state 검증 전에 조회, 검증 후 사용)
        String codeVerifier = null;
        if (PKCE_REQUIRED_PLUGINS.contains(pluginId)) {
            codeVerifier = pkceStorage.consumeCodeVerifier(state);
            if (codeVerifier == null) {
                log.warn("PKCE code_verifier not found for state: {}", state);
                return ResponseEntity.badRequest()
                        .body("Invalid PKCE state. Please try again.");
            }
        }

        if (!stateStorage.validateAndConsume(pluginId, state)) {
            log.warn("Invalid state for plugin {}: {}", pluginId, state);
            return ResponseEntity.badRequest()
                    .body("Invalid state. Please try again.");
        }

        try {
            String redirectUri = buildRedirectUri(request, pluginId);

            // PKCE code_verifier를 config에 추가
            PluginConfigInfo configWithPkce = config;
            if (codeVerifier != null) {
                Map<String, Object> metadata = new HashMap<>(config.metadata() != null ? config.metadata() : Map.of());
                metadata.put("code_verifier", codeVerifier);

                configWithPkce = PluginConfigInfo.builder()
                        .pluginId(config.pluginId())
                        .displayName(config.displayName())
                        .clientId(config.clientId())
                        .clientSecret(config.clientSecret())
                        .secrets(config.secrets())
                        .metadata(metadata)
                        .build();

                log.debug("PKCE code_verifier added for token exchange");
            }

            OAuthTokenInfo tokenInfo = pluginOAuthService.exchangeCode(pluginId, configWithPkce, code, redirectUri);

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

    /**
     * PKCE code_verifier 생성 (43-128자 URL-safe random string)
     */
    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[64];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    /**
     * PKCE code_challenge 생성 (code_verifier의 SHA256 해시)
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
