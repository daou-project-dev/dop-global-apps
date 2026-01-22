package com.daou.dop.global.apps.plugin.slack;

import com.daou.dop.global.apps.plugin.sdk.OAuthException;
import com.daou.dop.global.apps.plugin.sdk.OAuthHandler;
import com.daou.dop.global.apps.plugin.sdk.PluginConfig;
import com.daou.dop.global.apps.plugin.sdk.PluginMetadata;
import com.daou.dop.global.apps.plugin.sdk.TokenInfo;
import com.slack.api.Slack;
import com.slack.api.methods.response.oauth.OAuthV2AccessResponse;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Slack OAuth 핸들러 구현
 */
@Extension
public class SlackOAuthHandler implements OAuthHandler {

    private static final Logger log = LoggerFactory.getLogger(SlackOAuthHandler.class);
    private static final String PLUGIN_ID = "slack";
    private static final String OAUTH_AUTHORIZE_URL = "https://slack.com/oauth/v2/authorize";

    private final Slack slack;

    public SlackOAuthHandler() {
        this.slack = Slack.getInstance();
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.builder()
                .pluginId(PLUGIN_ID)
                .name("Slack")
                .description("Slack 워크스페이스 연동")
                .authType("OAUTH2")
                .iconUrl("https://a.slack-edge.com/80588/marketing/img/icons/icon_slack_hash_colored.png")
                .authUrl(OAUTH_AUTHORIZE_URL)
                .tokenUrl("https://slack.com/api/oauth.v2.access")
                .apiBaseUrl("https://slack.com/api")
                .defaultScopes("channels:history,channels:read,chat:write,chat:write.public,commands,app_mentions:read,im:history,im:read,im:write")
                // 민감 정보(clientId, clientSecret, secrets)는 DB에서 직접 관리
                .build();
    }

    @Override
    public String buildAuthorizationUrl(PluginConfig config, String state, String redirectUri) {
        // config에서 설정 조회 (DB에서 전달받음)
        String clientId = config.clientId();
        String scopes = config.getString("scopes");

        log.debug("Building authorization URL for plugin: {}, clientId: {}", PLUGIN_ID, clientId);

        return OAUTH_AUTHORIZE_URL +
                "?client_id=" + encode(clientId) +
                "&scope=" + encode(scopes != null ? scopes : "") +
                "&redirect_uri=" + encode(redirectUri) +
                "&state=" + encode(state);
    }

    @Override
    public TokenInfo exchangeCode(PluginConfig config, String code, String redirectUri) throws OAuthException {
        log.info("Exchanging code for token, plugin: {}", PLUGIN_ID);

        try {
            OAuthV2AccessResponse response = slack.methods().oauthV2Access(r -> r
                    .clientId(config.clientId())
                    .clientSecret(config.clientSecret())
                    .redirectUri(redirectUri)
                    .code(code)
            );

            if (!response.isOk()) {
                log.error("Slack OAuth failed: {}", response.getError());
                throw new OAuthException("SLACK_OAUTH_ERROR", "Slack OAuth failed: " + response.getError());
            }

            log.info("Successfully exchanged code for token, team: {}", response.getTeam().getName());

            Map<String, String> metadata = new HashMap<>();
            metadata.put("botUserId", response.getBotUserId());
            if (response.getAppId() != null) {
                metadata.put("appId", response.getAppId());
            }
            if (response.getEnterprise() != null && response.getEnterprise().getId() != null) {
                metadata.put("enterpriseId", response.getEnterprise().getId());
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

        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to exchange code for token", e);
            throw new OAuthException("TOKEN_EXCHANGE_FAILED", "Failed to exchange code: " + e.getMessage(), e);
        }
    }

    @Override
    public TokenInfo refreshToken(PluginConfig config, String refreshToken) throws OAuthException {
        log.info("Refreshing token for plugin: {}", PLUGIN_ID);

        try {
            var response = slack.methods().oauthV2Access(r -> r
                    .clientId(config.clientId())
                    .clientSecret(config.clientSecret())
                    .grantType("refresh_token")
                    .refreshToken(refreshToken)
            );

            if (!response.isOk()) {
                log.error("Slack token refresh failed: {}", response.getError());
                throw new OAuthException("TOKEN_REFRESH_FAILED", "Token refresh failed: " + response.getError());
            }

            log.info("Successfully refreshed token");

            return TokenInfo.builder()
                    .pluginId(PLUGIN_ID)
                    .externalId(response.getTeam().getId())
                    .externalName(response.getTeam().getName())
                    .accessToken(response.getAccessToken())
                    .refreshToken(response.getRefreshToken())
                    .scope(response.getScope())
                    .installedAt(Instant.now())
                    .build();

        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new OAuthException("TOKEN_REFRESH_FAILED", "Failed to refresh token: " + e.getMessage(), e);
        }
    }

    @Override
    public void revokeToken(PluginConfig config, String accessToken) throws OAuthException {
        log.info("Revoking token for plugin: {}", PLUGIN_ID);

        try {
            var response = slack.methods(accessToken).authRevoke(r -> r);

            if (!response.isOk()) {
                log.error("Slack token revoke failed: {}", response.getError());
                throw new OAuthException("TOKEN_REVOKE_FAILED", "Token revoke failed: " + response.getError());
            }

            log.info("Successfully revoked token");

        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to revoke token", e);
            throw new OAuthException("TOKEN_REVOKE_FAILED", "Failed to revoke token: " + e.getMessage(), e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
