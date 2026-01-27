package com.daou.dop.global.apps.plugin.ms365.calendar;

import com.daou.dop.global.apps.plugin.ms365.calendar.dto.UserProfile;
import com.daou.dop.global.apps.plugin.sdk.OAuthException;
import com.daou.dop.global.apps.plugin.sdk.OAuthHandler;
import com.daou.dop.global.apps.plugin.sdk.PluginConfig;
import com.daou.dop.global.apps.plugin.sdk.PluginMetadata;
import com.daou.dop.global.apps.plugin.sdk.TokenInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Microsoft 365 Calendar OAuth 핸들러 구현
 */
@Extension
public class Ms365CalendarOAuthHandler implements OAuthHandler {

    private static final Logger log = LoggerFactory.getLogger(Ms365CalendarOAuthHandler.class);
    private static final String PLUGIN_ID = "ms365-calendar";
    private static final String OAUTH_AUTHORIZE_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    private static final String TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    private static final String GRAPH_API_BASE_URL = "https://graph.microsoft.com/v1.0";
    private static final String DEFAULT_SCOPES = "User.Read Calendars.Read Calendars.ReadWrite offline_access";

    private final OkHttpClient httpClient;
    private final Gson gson;

    public Ms365CalendarOAuthHandler() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.builder()
                .pluginId(PLUGIN_ID)
                .name("Microsoft 365 Calendar")
                .description("Microsoft 365 캘린더 연동")
                .authType("OAUTH2")
                .iconUrl("https://upload.wikimedia.org/wikipedia/commons/d/df/Microsoft_Office_Outlook_%282018%E2%80%93present%29.svg")
                .authUrl(OAUTH_AUTHORIZE_URL)
                .tokenUrl(TOKEN_URL)
                .apiBaseUrl(GRAPH_API_BASE_URL)
                .defaultScopes(DEFAULT_SCOPES)
                .build();
    }

    @Override
    public String buildAuthorizationUrl(PluginConfig config, String state, String redirectUri) {
        String clientId = config.clientId();
        String scopes = config.getString("scopes");
        String codeChallenge = config.getString("code_challenge");
        String codeChallengeMethod = config.getString("code_challenge_method");

        log.debug("Building authorization URL for plugin: {}, clientId: {}", PLUGIN_ID, clientId);

        StringBuilder url = new StringBuilder(OAUTH_AUTHORIZE_URL)
                .append("?client_id=").append(encode(clientId))
                .append("&response_type=code")
                .append("&redirect_uri=").append(encode(redirectUri))
                .append("&response_mode=query")
                .append("&scope=").append(encode(scopes != null ? scopes : DEFAULT_SCOPES))
                .append("&state=").append(encode(state))
                .append("&prompt=select_account");

        // PKCE 파라미터 추가
        if (codeChallenge != null && codeChallengeMethod != null) {
            url.append("&code_challenge=").append(encode(codeChallenge))
               .append("&code_challenge_method=").append(encode(codeChallengeMethod));
            log.debug("PKCE parameters added to authorization URL");
        }

        return url.toString();
    }

    @Override
    public TokenInfo exchangeCode(PluginConfig config, String code, String redirectUri) throws OAuthException {
        log.info("Exchanging code for token, plugin: {}", PLUGIN_ID);

        try {
            String codeVerifier = config.getString("code_verifier");

            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("client_id", config.clientId())
                    .add("code", code)
                    .add("redirect_uri", redirectUri)
                    .add("grant_type", "authorization_code");

            // client_secret은 있을 경우에만 추가 (개인 계정은 client_secret 없이 PKCE만 사용 가능)
            if (config.clientSecret() != null && !config.clientSecret().isBlank()) {
                formBuilder.add("client_secret", config.clientSecret());
            }

            // PKCE code_verifier 추가
            if (codeVerifier != null) {
                formBuilder.add("code_verifier", codeVerifier);
                log.debug("PKCE code_verifier added to token request");
            }

            RequestBody formBody = formBuilder.build();

            Request request = new Request.Builder()
                    .url(TOKEN_URL)
                    .post(formBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    log.error("Token exchange failed: {}", responseBody);
                    throw new OAuthException("TOKEN_EXCHANGE_FAILED", "Token exchange failed: " + responseBody);
                }

                JsonObject tokenResponse = gson.fromJson(responseBody, JsonObject.class);
                String accessToken = tokenResponse.get("access_token").getAsString();
                String refreshToken = tokenResponse.has("refresh_token") ?
                        tokenResponse.get("refresh_token").getAsString() : null;
                String scope = tokenResponse.has("scope") ?
                        tokenResponse.get("scope").getAsString() : DEFAULT_SCOPES;
                int expiresIn = tokenResponse.has("expires_in") ?
                        tokenResponse.get("expires_in").getAsInt() : 3600;

                // 사용자 정보 조회
                UserProfile userProfile = fetchUserProfile(accessToken);

                log.info("Successfully exchanged code for token, user: {}", userProfile.displayName());

                Map<String, String> metadata = new HashMap<>();
                if (userProfile.mail() != null) {
                    metadata.put("email", userProfile.mail());
                }
                if (userProfile.jobTitle() != null) {
                    metadata.put("jobTitle", userProfile.jobTitle());
                }

                return TokenInfo.builder()
                        .pluginId(PLUGIN_ID)
                        .externalId(userProfile.id())
                        .externalName(userProfile.displayName())
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .scope(scope)
                        .expiresAt(Instant.now().plusSeconds(expiresIn))
                        .installedAt(Instant.now())
                        .metadata(metadata)
                        .build();
            }

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
            RequestBody formBody = new FormBody.Builder()
                    .add("client_id", config.clientId())
                    .add("client_secret", config.clientSecret())
                    .add("refresh_token", refreshToken)
                    .add("grant_type", "refresh_token")
                    .build();

            Request request = new Request.Builder()
                    .url(TOKEN_URL)
                    .post(formBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    log.error("Token refresh failed: {}", responseBody);
                    throw new OAuthException("TOKEN_REFRESH_FAILED", "Token refresh failed: " + responseBody);
                }

                JsonObject tokenResponse = gson.fromJson(responseBody, JsonObject.class);
                String newAccessToken = tokenResponse.get("access_token").getAsString();
                String newRefreshToken = tokenResponse.has("refresh_token") ?
                        tokenResponse.get("refresh_token").getAsString() : refreshToken;
                String scope = tokenResponse.has("scope") ?
                        tokenResponse.get("scope").getAsString() : DEFAULT_SCOPES;
                int expiresIn = tokenResponse.has("expires_in") ?
                        tokenResponse.get("expires_in").getAsInt() : 3600;

                // 사용자 정보 조회
                UserProfile userProfile = fetchUserProfile(newAccessToken);

                log.info("Successfully refreshed token");

                return TokenInfo.builder()
                        .pluginId(PLUGIN_ID)
                        .externalId(userProfile.id())
                        .externalName(userProfile.displayName())
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .scope(scope)
                        .expiresAt(Instant.now().plusSeconds(expiresIn))
                        .installedAt(Instant.now())
                        .build();
            }

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
        // Microsoft Graph API does not have a token revocation endpoint
        // Token will expire naturally or can be revoked through Azure AD portal
        log.info("Microsoft does not support programmatic token revocation. Token will expire naturally.");
    }

    @Override
    public boolean requiresPkce() {
        return true;
    }

    private UserProfile fetchUserProfile(String accessToken) throws IOException, OAuthException {
        Request request = new Request.Builder()
                .url(GRAPH_API_BASE_URL + "/me")
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                log.error("Failed to fetch user profile: {}", responseBody);
                throw new OAuthException("USER_PROFILE_FAILED", "Failed to fetch user profile: " + responseBody);
            }

            return gson.fromJson(responseBody, UserProfile.class);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
