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
    /**
     * 토큰 만료 여부 확인
     * - expiresAt이 NULL이고 refreshToken이 있으면 만료된 것으로 간주 (기존 연동 호환)
     * - expiresAt이 있으면 실제 만료 여부 확인
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            // expiresAt 없지만 refreshToken 있으면 갱신 필요 (기존 연동)
            return refreshToken != null && !refreshToken.isBlank();
        }
        return Instant.now().isAfter(expiresAt);
    }

    public String getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
}
