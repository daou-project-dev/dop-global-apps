package com.daou.dop.global.apps.core.execute.dto;

/**
 * 플러그인 API 실행 요청
 */
public record ExecuteRequest(
        String plugin,      // 플러그인 이름 (slack, etc.)
        String method,      // HTTP 메서드 (GET, POST, etc.)
        String uri,         // API URI (/chat.postMessage, etc.)
        String body,        // 요청 본문 (JSON)
        String teamId,      // 워크스페이스 ID (Slack Team ID)
        String accessToken  // 인증 토큰 (Server에서 주입)
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String plugin;
        private String method;
        private String uri;
        private String body;
        private String teamId;
        private String accessToken;

        public Builder plugin(String plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public ExecuteRequest build() {
            return new ExecuteRequest(plugin, method, uri, body, teamId, accessToken);
        }
    }
}
