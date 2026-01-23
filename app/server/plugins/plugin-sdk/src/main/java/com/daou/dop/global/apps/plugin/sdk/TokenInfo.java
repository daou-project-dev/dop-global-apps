package com.daou.dop.global.apps.plugin.sdk;

import java.time.Instant;
import java.util.Map;

/**
 * OAuth 토큰 정보 DTO
 * 플러그인 → 서버로 토큰 정보 전달
 *
 * @param pluginId     플러그인 ID
 * @param externalId   외부 시스템 ID (teamId, tenantId 등)
 * @param externalName 외부 시스템 표시명
 * @param accessToken  액세스 토큰
 * @param refreshToken 리프레시 토큰 (nullable)
 * @param scope        권한 범위
 * @param expiresAt    토큰 만료 시간 (nullable)
 * @param installedAt  설치 시간
 * @param metadata     추가 정보 (botUserId, appId 등)
 */
public record TokenInfo(
        String pluginId,
        String externalId,
        String externalName,
        String accessToken,
        String refreshToken,
        String scope,
        Instant expiresAt,
        Instant installedAt,
        Map<String, String> metadata
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pluginId;
        private String externalId;
        private String externalName;
        private String accessToken;
        private String refreshToken;
        private String scope;
        private Instant expiresAt;
        private Instant installedAt;
        private Map<String, String> metadata;

        public Builder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder externalName(String externalName) {
            this.externalName = externalName;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder installedAt(Instant installedAt) {
            this.installedAt = installedAt;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public TokenInfo build() {
            return new TokenInfo(
                    pluginId,
                    externalId,
                    externalName,
                    accessToken,
                    refreshToken,
                    scope,
                    expiresAt,
                    installedAt != null ? installedAt : Instant.now(),
                    metadata
            );
        }
    }
}
