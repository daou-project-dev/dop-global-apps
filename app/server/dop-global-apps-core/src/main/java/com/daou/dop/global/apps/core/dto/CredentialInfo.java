package com.daou.dop.global.apps.core.dto;

import lombok.Builder;

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
@Builder
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
}
