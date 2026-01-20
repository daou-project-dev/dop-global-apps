package com.daou.dop.global.apps.server.slack;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "slack.app")
public record SlackProperties(
        String clientId,
        String clientSecret,
        String signingSecret,
        String scopes,
        String redirectUri
) {
}
