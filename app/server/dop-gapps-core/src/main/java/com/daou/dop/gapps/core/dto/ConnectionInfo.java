package com.daou.dop.gapps.core.dto;

import com.daou.dop.gapps.core.enums.ScopeType;
import lombok.Builder;

/**
 * 연동 정보 (core DTO)
 *
 * @param id           연동 ID
 * @param pluginId     플러그인 ID
 * @param externalId   외부 시스템 ID
 * @param externalName 외부 시스템 표시명
 * @param scopeType    연동 범위
 * @param companyId    고객사 ID
 * @param userId       사용자 ID
 * @param active       활성 여부
 */
@Builder
public record ConnectionInfo(
        Long id,
        String pluginId,
        String externalId,
        String externalName,
        ScopeType scopeType,
        Long companyId,
        Long userId,
        boolean active
) {
}
