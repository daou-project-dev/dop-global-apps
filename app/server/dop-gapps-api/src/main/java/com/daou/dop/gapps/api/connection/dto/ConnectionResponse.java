package com.daou.dop.gapps.api.connection.dto;

import com.daou.dop.gapps.core.dto.ConnectionInfo;
import com.daou.dop.gapps.core.enums.ScopeType;

/**
 * Connection 목록 조회 응답 DTO
 *
 * @param id           Connection ID
 * @param pluginId     플러그인 ID
 * @param externalId   외부 시스템 ID (Slack Team ID 등)
 * @param externalName 표시용 이름 (Slack Workspace Name 등)
 * @param scopeType    연동 범위 타입
 * @param active       활성 상태
 */
public record ConnectionResponse(
        Long id,
        String pluginId,
        String externalId,
        String externalName,
        ScopeType scopeType,
        boolean active
) {
    /**
     * Core DTO → API DTO 변환
     */
    public static ConnectionResponse from(ConnectionInfo info) {
        return new ConnectionResponse(
                info.id(),
                info.pluginId(),
                info.externalId(),
                info.externalName(),
                info.scopeType(),
                info.active()
        );
    }
}
