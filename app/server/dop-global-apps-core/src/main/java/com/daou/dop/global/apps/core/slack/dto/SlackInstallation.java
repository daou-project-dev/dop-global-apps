package com.daou.dop.global.apps.core.slack.dto;

import java.time.Instant;

/**
 * Slack 워크스페이스 설치 정보 DTO
 */
public record SlackInstallation(
        String teamId,
        String teamName,
        String accessToken,
        String botUserId,
        String scope,
        Instant installedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String teamId;
        private String teamName;
        private String accessToken;
        private String botUserId;
        private String scope;
        private Instant installedAt;

        public Builder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public Builder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder botUserId(String botUserId) {
            this.botUserId = botUserId;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder installedAt(Instant installedAt) {
            this.installedAt = installedAt;
            return this;
        }

        public SlackInstallation build() {
            return new SlackInstallation(teamId, teamName, accessToken, botUserId, scope, installedAt);
        }
    }
}
