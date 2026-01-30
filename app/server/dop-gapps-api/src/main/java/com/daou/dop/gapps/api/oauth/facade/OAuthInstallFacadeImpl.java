package com.daou.dop.gapps.api.oauth.facade;

import com.daou.dop.gapps.core.connection.ConnectionService;
import com.daou.dop.gapps.core.dto.OAuthTokenInfo;
import com.daou.dop.gapps.core.dto.PluginConfigInfo;
import com.daou.dop.gapps.core.oauth.OAuthException;
import com.daou.dop.gapps.core.oauth.PkceService;
import com.daou.dop.gapps.core.oauth.PluginOAuthService;
import com.daou.dop.gapps.core.oauth.StateStorage;
import com.daou.dop.gapps.core.plugin.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth 설치 Facade 구현
 */
@Service
public class OAuthInstallFacadeImpl implements OAuthInstallFacade {

    private static final Logger log = LoggerFactory.getLogger(OAuthInstallFacadeImpl.class);
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final PluginOAuthService pluginOAuthService;
    private final PluginService pluginService;
    private final ConnectionService connectionService;
    private final StateStorage stateStorage;
    private final PkceService pkceService;

    public OAuthInstallFacadeImpl(
            PluginOAuthService pluginOAuthService,
            PluginService pluginService,
            ConnectionService connectionService,
            StateStorage stateStorage,
            PkceService pkceService) {
        this.pluginOAuthService = pluginOAuthService;
        this.pluginService = pluginService;
        this.connectionService = connectionService;
        this.stateStorage = stateStorage;
        this.pkceService = pkceService;
    }

    @Override
    public String startInstall(String pluginId, String redirectUri) {
        validatePluginSupport(pluginId);
        PluginConfigInfo config = getPluginConfig(pluginId);

        String state = stateStorage.generateAndStore(pluginId, STATE_TTL);
        PluginConfigInfo configWithPkce = preparePkceConfig(pluginId, config, state);

        String authorizationUrl = pluginOAuthService.buildAuthorizationUrl(
                pluginId, configWithPkce, state, redirectUri);

        log.info("Starting OAuth for plugin: {}", pluginId);
        return authorizationUrl;
    }

    @Override
    public Long handleCallback(String pluginId, String code, String state, String redirectUri) {
        validatePluginSupport(pluginId);
        PluginConfigInfo config = getPluginConfig(pluginId);

        String codeVerifier = consumePkceVerifier(pluginId, state);
        validateState(pluginId, state);

        PluginConfigInfo configWithPkce = addCodeVerifierToConfig(config, codeVerifier);

        try {
            OAuthTokenInfo tokenInfo = pluginOAuthService.exchangeCode(
                    pluginId, configWithPkce, code, redirectUri);

            Long connectionId = connectionService.saveOAuthToken(tokenInfo);

            log.info("OAuth successful for plugin {}: {} ({}) - connectionId={}",
                    pluginId, tokenInfo.externalName(), tokenInfo.externalId(), connectionId);

            return connectionId;
        } catch (OAuthException e) {
            log.error("OAuth failed for plugin {}: {}", pluginId, e.getMessage(), e);
            throw new OAuthInstallException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Installation failed: " + e.getMessage(), e);
        }
    }

    private void validatePluginSupport(String pluginId) {
        if (!pluginOAuthService.supportsOAuth(pluginId)) {
            log.warn("OAuthHandler not found for plugin: {}", pluginId);
            throw new OAuthInstallException(HttpStatus.NOT_FOUND, "Unknown plugin: " + pluginId);
        }
    }

    private PluginConfigInfo getPluginConfig(String pluginId) {
        return pluginService.getPluginConfig(pluginId)
                .orElseThrow(() -> {
                    log.warn("Plugin config not found in DB: {}", pluginId);
                    return new OAuthInstallException(HttpStatus.NOT_FOUND,
                            "Plugin not configured: " + pluginId);
                });
    }

    private PluginConfigInfo preparePkceConfig(String pluginId, PluginConfigInfo config, String state) {
        if (!pluginOAuthService.requiresPkce(pluginId)) {
            return config;
        }

        String codeChallenge = pkceService.generateAndStoreCodeChallenge(state, STATE_TTL);

        Map<String, Object> metadata = new HashMap<>(config.metadata() != null ? config.metadata() : Map.of());
        metadata.put("code_challenge", codeChallenge);
        metadata.put("code_challenge_method", "S256");

        log.debug("PKCE enabled for plugin: {}", pluginId);

        return PluginConfigInfo.builder()
                .pluginId(config.pluginId())
                .displayName(config.displayName())
                .clientId(config.clientId())
                .clientSecret(config.clientSecret())
                .secrets(config.secrets())
                .metadata(metadata)
                .build();
    }

    private String consumePkceVerifier(String pluginId, String state) {
        if (!pluginOAuthService.requiresPkce(pluginId)) {
            return null;
        }

        String codeVerifier = pkceService.consumeCodeVerifier(state);
        if (codeVerifier == null) {
            log.warn("PKCE code_verifier not found for state: {}", state);
            throw new OAuthInstallException(HttpStatus.BAD_REQUEST,
                    "Invalid PKCE state. Please try again.");
        }
        return codeVerifier;
    }

    private void validateState(String pluginId, String state) {
        if (!stateStorage.validateAndConsume(pluginId, state)) {
            log.warn("Invalid state for plugin {}: {}", pluginId, state);
            throw new OAuthInstallException(HttpStatus.BAD_REQUEST,
                    "Invalid state. Please try again.");
        }
    }

    private PluginConfigInfo addCodeVerifierToConfig(PluginConfigInfo config, String codeVerifier) {
        if (codeVerifier == null) {
            return config;
        }

        Map<String, Object> metadata = new HashMap<>(config.metadata() != null ? config.metadata() : Map.of());
        metadata.put("code_verifier", codeVerifier);

        log.debug("PKCE code_verifier added for token exchange");

        return PluginConfigInfo.builder()
                .pluginId(config.pluginId())
                .displayName(config.displayName())
                .clientId(config.clientId())
                .clientSecret(config.clientSecret())
                .secrets(config.secrets())
                .metadata(metadata)
                .build();
    }
}
