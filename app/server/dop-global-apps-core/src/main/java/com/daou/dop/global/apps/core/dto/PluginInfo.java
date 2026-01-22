package com.daou.dop.global.apps.core.dto;

/**
 * 플러그인 정보 (api 표시용)
 *
 * @param pluginId    플러그인 식별자
 * @param name        표시명
 * @param description 설명
 * @param iconUrl     아이콘 URL
 * @param authType    인증 방식
 * @param active      활성 여부
 */
public record PluginInfo(
        String pluginId,
        String name,
        String description,
        String iconUrl,
        String authType,
        boolean active
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pluginId;
        private String name;
        private String description;
        private String iconUrl;
        private String authType;
        private boolean active;

        public Builder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public PluginInfo build() {
            return new PluginInfo(pluginId, name, description, iconUrl, authType, active);
        }
    }
}
