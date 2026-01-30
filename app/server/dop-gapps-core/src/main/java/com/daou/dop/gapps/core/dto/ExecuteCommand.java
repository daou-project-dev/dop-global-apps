package com.daou.dop.gapps.core.dto;

import java.util.Map;

/**
 * 플러그인 API 실행 요청 (api → core)
 *
 * @param pluginId 플러그인 식별자
 * @param action   실행할 액션
 * @param params   액션 파라미터
 */
public record ExecuteCommand(
        String pluginId,
        String action,
        Map<String, Object> params
) {
    public String getStringParam(String key) {
        if (params == null) return null;
        Object value = params.get(key);
        return value != null ? value.toString() : null;
    }
}
