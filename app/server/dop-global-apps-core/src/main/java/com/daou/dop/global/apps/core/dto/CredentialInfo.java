package com.daou.dop.global.apps.core.dto;

import java.time.Instant;
import java.util.Map;

/**
 * 인증 정보 (core ↔ api)
 * CredentialProvider가 반환, ConnectionService가 생성
 *
 * @param accessToken  OAuth Access Token
 * @param refreshToken OAuth Refresh Token
 * @param apiKey       API Key
 * @param expiresAt    토큰 만료 시간
 * @param externalId   외부 시스템 ID
 * @param metadata     추가 정보
 */
public record CredentialInfo(
        String accessToken,
        String refreshToken,
        String apiKey,
        Instant expiresAt,
        String externalId,
        Map<String, String> metadata
) {
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public String getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String apiKey;
        private Instant expiresAt;
        private String externalId;
        private Map<String, String> metadata;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public CredentialInfo build() {
            return new CredentialInfo(accessToken, refreshToken, apiKey, expiresAt, externalId, metadata);
        }
    }
}
