package com.daou.dop.global.apps.core.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 플러그인 설정 정보 (core ↔ api)
 *
 * @param pluginId     플러그인 식별자
 * @param displayName  표시명
 * @param clientId     OAuth Client ID
 * @param clientSecret OAuth Client Secret
 * @param secrets      추가 민감 정보
 * @param metadata     설정 정보
 */
@Builder
public record PluginConfigInfo(
        String pluginId,
        String displayName,
        String clientId,
        String clientSecret,
        Map<String, String> secrets,
        Map<String, Object> metadata
) {
    public String getString(String key) {
        if (metadata == null) return null;
        Object value = metadata.get(key);
        return value != null ? value.toString() : null;
    }

    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

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

    public String getSecret(String key) {
        return secrets != null ? secrets.get(key) : null;
    }
}
