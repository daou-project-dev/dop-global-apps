package com.daou.dop.gapps.api.connection.dto;

/**
 * 간단한 연동 생성 요청
 */
public record CreateConnectionRequest(
        String pluginId,
        String externalId,
        String externalName
) {
}
