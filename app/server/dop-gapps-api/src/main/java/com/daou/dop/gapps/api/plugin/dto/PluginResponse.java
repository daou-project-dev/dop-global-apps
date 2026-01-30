package com.daou.dop.gapps.api.plugin.dto;

import com.daou.dop.gapps.core.dto.PluginInfo;

/**
 * 플러그인 목록 조회 응답 DTO
 *
 * @param pluginId    플러그인 고유 식별자 (예: "slack", "google-calendar")
 * @param name        플러그인 표시명
 * @param description 플러그인 설명
 * @param iconUrl     아이콘 URL
 * @param authType    인증 방식 (OAUTH2, API_KEY, SERVICE_ACCOUNT)
 * @param active      활성 상태
 * @param authConfig  인증 설정 정보
 */
public record PluginResponse(
        String pluginId,
        String name,
        String description,
        String iconUrl,
        String authType,
        boolean active,
        AuthConfig authConfig
) {
    /**
     * Core DTO → API DTO 변환
     */
    public static PluginResponse from(PluginInfo info) {
        return new PluginResponse(
                info.pluginId(),
                info.name(),
                info.description(),
                info.iconUrl(),
                info.authType(),
                info.active(),
                AuthConfig.from(info.authConfig())
        );
    }
}
