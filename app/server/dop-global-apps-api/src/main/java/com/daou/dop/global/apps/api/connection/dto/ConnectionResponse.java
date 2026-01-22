package com.daou.dop.global.apps.api.connection.dto;

import com.daou.dop.global.apps.domain.connection.PluginConnection;
import com.daou.dop.global.apps.domain.enums.ConnectionStatus;
import com.daou.dop.global.apps.domain.enums.ScopeType;

import java.time.Instant;

/**
 * Connection 목록 조회 응답 DTO
 *
 * @param id           Connection ID
 * @param pluginId     플러그인 ID
 * @param externalId   외부 시스템 ID (Slack Team ID 등)
 * @param externalName 표시용 이름 (Slack Workspace Name 등)
 * @param scopeType    연동 범위 타입
 * @param status       연동 상태
 * @param createdAt    생성일시
 */
public record ConnectionResponse(
        Long id,
        String pluginId,
        String externalId,
        String externalName,
        ScopeType scopeType,
        ConnectionStatus status,
        Instant createdAt
) {
    /**
     * Entity → DTO 변환
     */
    public static ConnectionResponse from(PluginConnection connection) {
        return new ConnectionResponse(
                connection.getId(),
                connection.getPluginId(),
                connection.getExternalId(),
                connection.getExternalName(),
                connection.getScopeType(),
                connection.getStatus(),
                connection.getCreatedAt()
        );
    }
}
