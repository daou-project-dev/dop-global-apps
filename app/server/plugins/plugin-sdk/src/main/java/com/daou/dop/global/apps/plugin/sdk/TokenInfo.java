package com.daou.dop.global.apps.plugin.sdk;

import lombok.Builder;

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
@Builder
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
}
