package com.daou.dop.global.apps.plugin.sdk;

import java.util.Map;

/**
 * 플러그인 메타데이터
 * 플러그인이 자신의 정보를 서버에 제공하기 위한 DTO
 *
 * <p>플러그인 JAR에서 모든 설정을 제공:
 * - 기본 정보: pluginId, name, description, authType
 * - OAuth 설정: authUrl, tokenUrl, clientId, clientSecret, defaultScopes
 * - 추가 설정: secrets, metadata
 */
public record PluginMetadata(
        String pluginId,
        String name,
        String description,
        String authType,          // "OAUTH2", "API_KEY", "NONE"
        String iconUrl,
        String authUrl,           // OAuth authorization URL
        String tokenUrl,          // OAuth token exchange URL
        String apiBaseUrl,        // API base URL
        String defaultScopes,     // 기본 OAuth scopes
        String clientId,          // OAuth Client ID
        String clientSecret,      // OAuth Client Secret
        Map<String, String> secrets,  // 추가 민감 정보 (signing_secret 등)
        Map<String, Object> metadata  // 추가 메타데이터
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pluginId;
        private String name;
        private String description;
        private String authType = "OAUTH2";
        private String iconUrl;
        private String authUrl;
        private String tokenUrl;
        private String apiBaseUrl;
        private String defaultScopes;
        private String clientId;
        private String clientSecret;
        private Map<String, String> secrets;
        private Map<String, Object> metadata;

        public Builder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder authUrl(String authUrl) {
            this.authUrl = authUrl;
            return this;
        }

        public Builder tokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
            return this;
        }

        public Builder apiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
            return this;
        }

        public Builder defaultScopes(String defaultScopes) {
            this.defaultScopes = defaultScopes;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder secrets(Map<String, String> secrets) {
            this.secrets = secrets;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public PluginMetadata build() {
            return new PluginMetadata(
                    pluginId, name, description, authType,
                    iconUrl, authUrl, tokenUrl, apiBaseUrl,
                    defaultScopes, clientId, clientSecret, secrets, metadata
            );
        }
    }
}
