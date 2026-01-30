package com.daou.dop.global.apps.plugin.jira;

import com.daou.dop.global.apps.plugin.sdk.OAuthException;
import com.daou.dop.global.apps.plugin.sdk.OAuthHandler;
import com.daou.dop.global.apps.plugin.sdk.PluginConfig;
import com.daou.dop.global.apps.plugin.sdk.PluginMetadata;
import com.daou.dop.global.apps.plugin.sdk.TokenInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Jira Cloud OAuth 2.0 (3LO) 핸들러
 *
 * <p>Atlassian OAuth 2.0 (3-legged OAuth) 구현
 * <p>참고: https://developer.atlassian.com/cloud/jira/platform/oauth-2-3lo-apps/
 */
@Extension
public class JiraOAuthHandler implements OAuthHandler {

    private static final Logger log = LoggerFactory.getLogger(JiraOAuthHandler.class);
    private static final String PLUGIN_ID = "jira";

    private static final String OAUTH_AUTHORIZE_URL = "https://auth.atlassian.com/authorize";
    private static final String OAUTH_TOKEN_URL = "https://auth.atlassian.com/oauth/token";
    private static final String ACCESSIBLE_RESOURCES_URL = "https://api.atlassian.com/oauth/token/accessible-resources";

    private static final String DEFAULT_SCOPES = "read:jira-user read:jira-work write:jira-work offline_access";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JiraOAuthHandler() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.builder()
                .pluginId(PLUGIN_ID)
                .name("Jira")
                .description("Jira Cloud 연동")
                .authType("OAUTH2")
                .iconUrl("https://wac-cdn.atlassian.com/assets/img/favicons/atlassian/favicon.png")
                .authUrl(OAUTH_AUTHORIZE_URL)
                .tokenUrl(OAUTH_TOKEN_URL)
                .apiBaseUrl("https://api.atlassian.com")
                .defaultScopes(DEFAULT_SCOPES)
                .build();
    }

    @Override
    public String buildAuthorizationUrl(PluginConfig config, String state, String redirectUri) {
        String clientId = config.clientId();
        String scopes = config.getString("scopes");
        if (scopes == null || scopes.isBlank()) {
            scopes = DEFAULT_SCOPES;
        }

        log.debug("Building Jira authorization URL, clientId: {}", clientId);

        return OAUTH_AUTHORIZE_URL +
                "?audience=api.atlassian.com" +
                "&client_id=" + encode(clientId) +
                "&scope=" + encode(scopes) +
                "&redirect_uri=" + encode(redirectUri) +
                "&state=" + encode(state) +
                "&response_type=code" +
                "&prompt=consent";
    }

    @Override
    public TokenInfo exchangeCode(PluginConfig config, String code, String redirectUri) throws OAuthException {
        log.info("Exchanging code for token, plugin: {}", PLUGIN_ID);

        try {
            // 1. Token 교환
            String tokenRequestBody = "grant_type=authorization_code" +
                    "&client_id=" + encode(config.clientId()) +
                    "&client_secret=" + encode(config.clientSecret()) +
                    "&code=" + encode(code) +
                    "&redirect_uri=" + encode(redirectUri);

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(OAUTH_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

            if (tokenResponse.statusCode() != 200) {
                log.error("Token exchange failed: {}", tokenResponse.body());
                throw new OAuthException("TOKEN_EXCHANGE_FAILED", "Token exchange failed: " + tokenResponse.body());
            }

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());
            String accessToken = tokenJson.get("access_token").asText();
            String refreshToken = tokenJson.has("refresh_token") ? tokenJson.get("refresh_token").asText() : null;
            String scope = tokenJson.has("scope") ? tokenJson.get("scope").asText() : null;

            // expires_in 파싱 (기본값: 3600초 = 1시간)
            int expiresIn = tokenJson.has("expires_in") ? tokenJson.get("expires_in").asInt() : 3600;
            Instant expiresAt = Instant.now().plusSeconds(expiresIn);

            // 2. Accessible Resources 조회 (Cloud ID 획득)
            HttpRequest resourcesRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ACCESSIBLE_RESOURCES_URL))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> resourcesResponse = httpClient.send(resourcesRequest, HttpResponse.BodyHandlers.ofString());

            if (resourcesResponse.statusCode() != 200) {
                log.error("Failed to get accessible resources: {}", resourcesResponse.body());
                throw new OAuthException("RESOURCE_FETCH_FAILED", "Failed to get accessible resources");
            }

            JsonNode resourcesJson = objectMapper.readTree(resourcesResponse.body());

            if (!resourcesJson.isArray() || resourcesJson.isEmpty()) {
                throw new OAuthException("NO_ACCESSIBLE_SITES", "No accessible Jira sites found");
            }

            // 첫 번째 사이트 사용 (대부분 하나의 사이트만 있음)
            JsonNode firstSite = resourcesJson.get(0);
            String cloudId = firstSite.get("id").asText();
            String siteName = firstSite.get("name").asText();
            String siteUrl = firstSite.get("url").asText();

            log.info("Successfully obtained Jira access, site: {} ({})", siteName, cloudId);

            // URL에서 도메인 추출 (예: https://mycompany.atlassian.net -> mycompany)
            String externalId = extractDomainFromUrl(siteUrl);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("cloudId", cloudId);
            metadata.put("siteUrl", siteUrl);

            return TokenInfo.builder()
                    .pluginId(PLUGIN_ID)
                    .externalId(externalId)
                    .externalName(siteName)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .scope(scope)
                    .expiresAt(expiresAt)
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
        log.info("Refreshing Jira token");

        try {
            String requestBody = "grant_type=refresh_token" +
                    "&client_id=" + encode(config.clientId()) +
                    "&client_secret=" + encode(config.clientSecret()) +
                    "&refresh_token=" + encode(refreshToken);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OAUTH_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Token refresh failed: {}", response.body());
                throw new OAuthException("TOKEN_REFRESH_FAILED", "Token refresh failed: " + response.body());
            }

            JsonNode json = objectMapper.readTree(response.body());
            String newAccessToken = json.get("access_token").asText();
            String newRefreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : refreshToken;
            String scope = json.has("scope") ? json.get("scope").asText() : null;

            // expires_in 파싱 (기본값: 3600초 = 1시간)
            int expiresIn = json.has("expires_in") ? json.get("expires_in").asInt() : 3600;
            Instant expiresAt = Instant.now().plusSeconds(expiresIn);

            log.info("Successfully refreshed Jira token, expires in {} seconds", expiresIn);

            return TokenInfo.builder()
                    .pluginId(PLUGIN_ID)
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .scope(scope)
                    .expiresAt(expiresAt)
                    .installedAt(Instant.now())
                    .build();

        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new OAuthException("TOKEN_REFRESH_FAILED", "Failed to refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * URL에서 도메인 추출
     * 예: https://mycompany.atlassian.net -> mycompany
     */
    private String extractDomainFromUrl(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host != null && host.endsWith(".atlassian.net")) {
                return host.replace(".atlassian.net", "");
            }
            return host;
        } catch (Exception e) {
            log.warn("Failed to extract domain from URL: {}", url);
            return url;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
