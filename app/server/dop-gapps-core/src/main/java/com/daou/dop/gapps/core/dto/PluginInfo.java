package com.daou.dop.gapps.core.dto;

import lombok.Builder;

/**
 * 플러그인 정보 (api 표시용)
 *
 * @param pluginId    플러그인 식별자
 * @param name        표시명
 * @param description 설명
 * @param iconUrl     아이콘 URL
 * @param authType    인증 방식
 * @param active      활성 여부
 * @param authConfig  인증 설정 정보
 */
@Builder
public record PluginInfo(
        String pluginId,
        String name,
        String description,
        String iconUrl,
        String authType,
        boolean active,
        AuthConfigInfo authConfig
) {
}
