package com.daou.dop.global.apps.plugin.sdk;

import java.util.Map;
import lombok.Builder;

/**
 * 플러그인 메타데이터
 * 플러그인이 자신의 정보를 서버에 제공하기 위한 DTO
 *
 * <p>플러그인 JAR에서 모든 설정을 제공:
 * - 기본 정보: pluginId, name, description, authType
 * - OAuth 설정: authUrl, tokenUrl, clientId, clientSecret, defaultScopes
 * - 추가 설정: secrets, metadata
 */
@Builder
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
}
