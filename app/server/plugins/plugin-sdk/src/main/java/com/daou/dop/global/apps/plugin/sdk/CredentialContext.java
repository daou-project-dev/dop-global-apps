package com.daou.dop.global.apps.plugin.sdk;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

/**
 * API 실행용 인증 정보
 * DB의 oauth_credential/apikey_credential 테이블에서 조회
 *
 * @param accessToken  OAuth Access Token (복호화됨)
 * @param refreshToken OAuth Refresh Token (복호화됨, nullable)
 * @param apiKey       API Key (복호화됨, OAuth면 null)
 * @param expiresAt    토큰 만료 시간 (nullable)
 * @param externalId   외부 시스템 ID (teamId, tenantId 등)
 * @param metadata     추가 정보 (botUserId 등)
 */
@Builder
public record CredentialContext(
        String accessToken,
        String refreshToken,
        String apiKey,
        Instant expiresAt,
        String externalId,
        Map<String, String> metadata
) {
    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * metadata에서 값 조회
     */
    public String getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * OAuth 인증인지 확인
     */
    public boolean isOAuth() {
        return accessToken != null && !accessToken.isBlank();
    }

    /**
     * API Key 인증인지 확인
     */
    public boolean isApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
