package com.daou.dop.global.apps.api.plugin.dto;

import com.daou.dop.global.apps.domain.enums.AuthType;
import com.daou.dop.global.apps.domain.enums.PluginStatus;
import com.daou.dop.global.apps.domain.plugin.Plugin;

/**
 * 플러그인 목록 조회 응답 DTO
 *
 * <p>Entity의 민감 정보(clientSecret 등)를 제외하고 필요한 필드만 전달
 *
 * @param pluginId    플러그인 고유 식별자 (예: "slack", "google-calendar")
 * @param name        플러그인 표시명
 * @param description 플러그인 설명
 * @param iconUrl     아이콘 URL
 * @param authType    인증 방식 (OAUTH2, API_KEY, BASIC 등)
 * @param status      플러그인 상태 (ACTIVE, INACTIVE)
 */
public record PluginResponse(
        String pluginId,
        String name,
        String description,
        String iconUrl,
        AuthType authType,
        PluginStatus status
) {
    /**
     * Entity → DTO 변환
     */
    public static PluginResponse from(Plugin plugin) {
        return new PluginResponse(
                plugin.getPluginId(),
                plugin.getName(),
                plugin.getDescription(),
                plugin.getIconUrl(),
                plugin.getAuthType(),
                plugin.getStatus()
        );
    }
}
