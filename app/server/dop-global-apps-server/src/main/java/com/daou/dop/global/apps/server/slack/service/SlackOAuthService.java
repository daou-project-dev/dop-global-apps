package com.daou.dop.global.apps.server.slack.service;

import com.daou.dop.global.apps.core.slack.dto.SlackInstallation;
import com.daou.dop.global.apps.server.slack.SlackProperties;
import com.slack.api.Slack;
import com.slack.api.methods.response.oauth.OAuthV2AccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Slack OAuth 2.0 처리
 */
@Service
@EnableConfigurationProperties(SlackProperties.class)
public class SlackOAuthService {

    private static final Logger log = LoggerFactory.getLogger(SlackOAuthService.class);
    private static final String OAUTH_AUTHORIZE_URL = "https://slack.com/oauth/v2/authorize";

    private final SlackProperties properties;
    private final SlackTokenService tokenService;
    private final Slack slack;

    public SlackOAuthService(SlackProperties properties, SlackTokenService tokenService) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.slack = Slack.getInstance();
    }

    /**
     * OAuth 설치 URL 생성
     */
    public String getInstallUrl() {
        String state = UUID.randomUUID().toString();

        return OAUTH_AUTHORIZE_URL +
                "?client_id=" + encode(properties.clientId()) +
                "&scope=" + encode(properties.scopes()) +
                "&redirect_uri=" + encode(properties.redirectUri()) +
                "&state=" + encode(state);
    }

    /**
     * OAuth 콜백 처리
     */
    public void handleCallback(String code, String state) throws Exception {
        OAuthV2AccessResponse response = slack.methods().oauthV2Access(r -> r
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .redirectUri(properties.redirectUri())
                .code(code)
        );

        if (!response.isOk()) {
            log.error("OAuth failed: {}", response.getError());
            throw new RuntimeException("OAuth failed: " + response.getError());
        }

        SlackInstallation installation = SlackInstallation.builder()
                .teamId(response.getTeam().getId())
                .teamName(response.getTeam().getName())
                .accessToken(response.getAccessToken())
                .botUserId(response.getBotUserId())
                .scope(response.getScope())
                .installedAt(Instant.now())
                .build();

        tokenService.save(installation);

        log.info("Successfully installed Slack app for team: {} ({})",
                response.getTeam().getName(), response.getTeam().getId());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
