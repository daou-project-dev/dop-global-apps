package com.daou.dop.global.apps.plugin.slack;

import com.daou.dop.global.apps.core.oauth.OAuthException;
import com.daou.dop.global.apps.core.oauth.OAuthHandler;
import com.daou.dop.global.apps.core.oauth.TokenInfo;
import com.slack.api.Slack;
import com.slack.api.methods.response.oauth.OAuthV2AccessResponse;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Slack OAuth 핸들러 구현
 */
@Extension
public class SlackOAuthHandler implements OAuthHandler {

    private static final Logger log = LoggerFactory.getLogger(SlackOAuthHandler.class);
    private static final String PLUGIN_ID = "slack";
    private static final String OAUTH_AUTHORIZE_URL = "https://slack.com/oauth/v2/authorize";

    private final Slack slack;
    private final Properties slackProperties;

    public SlackOAuthHandler() {
        this.slack = Slack.getInstance();
        this.slackProperties = loadProperties();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("slack.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                log.warn("slack.properties not found, using system properties");
            }
        } catch (IOException e) {
            log.error("Failed to load slack.properties", e);
        }
        return props;
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public String buildAuthorizationUrl(String state, String redirectUri) {
        String clientId = getProperty("slack.clientId");
        String scopes = getProperty("slack.scopes");

        return OAUTH_AUTHORIZE_URL +
                "?client_id=" + encode(clientId) +
                "&scope=" + encode(scopes) +
                "&redirect_uri=" + encode(redirectUri) +
                "&state=" + encode(state);
    }

    @Override
    public TokenInfo exchangeCode(String code, String redirectUri) throws OAuthException {
        String clientId = getProperty("slack.clientId");
        String clientSecret = getProperty("slack.clientSecret");

        try {
            OAuthV2AccessResponse response = slack.methods().oauthV2Access(r -> r
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .code(code)
            );

            if (!response.isOk()) {
                log.error("Slack OAuth failed: {}", response.getError());
                throw new OAuthException("SLACK_OAUTH_ERROR", "Slack OAuth failed: " + response.getError());
            }

            Map<String, String> metadata = new HashMap<>();
            metadata.put("botUserId", response.getBotUserId());
            if (response.getAppId() != null) {
                metadata.put("appId", response.getAppId());
            }

            return TokenInfo.builder()
                    .pluginId(PLUGIN_ID)
                    .externalId(response.getTeam().getId())
                    .externalName(response.getTeam().getName())
                    .accessToken(response.getAccessToken())
                    .refreshToken(response.getRefreshToken())
                    .scope(response.getScope())
                    .installedAt(Instant.now())
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.error("Failed to exchange code for token", e);
            throw new OAuthException("TOKEN_EXCHANGE_FAILED", "Failed to exchange code: " + e.getMessage(), e);
        }
    }

    private String getProperty(String key) {
        // 우선순위: 환경변수 > 시스템 프로퍼티 > slack.properties
        String envKey = key.toUpperCase().replace(".", "_");
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            return value;
        }

        value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        return slackProperties.getProperty(key, "");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
