package com.daou.dop.gapps.plugin.sdk;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 플러그인 설정 정보
 * DB의 plugin 테이블에서 조회하여 전달
 *
 * @param pluginId     플러그인 식별자 (slack, google, etc.)
 * @param clientId     OAuth Client ID
 * @param clientSecret OAuth Client Secret (복호화됨)
 * @param secrets      추가 민감 정보 (signing_secret 등)
 * @param metadata     설정 정보 (scopes, authUrl, tokenUrl 등)
 */
@Builder
public record PluginConfig(
        String pluginId,
        String clientId,
        String clientSecret,
        Map<String, String> secrets,
        Map<String, Object> metadata
) {
    /**
     * metadata에서 String 값 조회
     */
    public String getString(String key) {
        if (metadata == null) return null;
        Object value = metadata.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * metadata에서 String 값 조회 (기본값 지정)
     */
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

    /**
     * metadata에서 List<String> 값 조회
     */
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        if (metadata == null) return List.of();
        Object value = metadata.get(key);
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    /**
     * secrets에서 값 조회
     */
    public String getSecret(String key) {
        return secrets != null ? secrets.get(key) : null;
    }
}
