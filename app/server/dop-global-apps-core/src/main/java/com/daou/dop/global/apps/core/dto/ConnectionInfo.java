package com.daou.dop.global.apps.core.dto;

import com.daou.dop.global.apps.core.enums.ScopeType;

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
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String pluginId;
        private String externalId;
        private String externalName;
        private ScopeType scopeType;
        private Long companyId;
        private Long userId;
        private boolean active;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder externalName(String externalName) {
            this.externalName = externalName;
            return this;
        }

        public Builder scopeType(ScopeType scopeType) {
            this.scopeType = scopeType;
            return this;
        }

        public Builder companyId(Long companyId) {
            this.companyId = companyId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public ConnectionInfo build() {
            return new ConnectionInfo(id, pluginId, externalId, externalName, scopeType, companyId, userId, active);
        }
    }
}
