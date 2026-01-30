package com.daou.dop.gapps.plugin.sdk;

import lombok.Builder;

import java.util.Map;

/**
 * 플러그인 API 실행 요청
 *
 * @param pluginId   플러그인 식별자
 * @param action     실행할 액션 (chat.postMessage, send-email 등)
 * @param params     액션 파라미터
 * @param credential 인증 정보
 */
@Builder
public record ExecuteRequest(
        String pluginId,
        String action,
        Map<String, Object> params,
        CredentialContext credential
) {
    /**
     * 파라미터에서 String 값 조회
     */
    public String getStringParam(String key) {
        if (params == null) return null;
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 파라미터에서 값 조회 (타입 캐스팅)
     */
    @SuppressWarnings("unchecked")
    public <T> T getParam(String key, Class<T> type) {
        if (params == null) return null;
        Object value = params.get(key);
        return type.isInstance(value) ? (T) value : null;
    }

    /**
     * 파라미터에서 Integer 값 조회
     */
    public Integer getIntParam(String key) {
        Object value = params != null ? params.get(key) : null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 파라미터에서 Boolean 값 조회
     */
    public Boolean getBooleanParam(String key) {
        Object value = params != null ? params.get(key) : null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }
}
