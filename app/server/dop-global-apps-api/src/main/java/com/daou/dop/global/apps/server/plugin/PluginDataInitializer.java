package com.daou.dop.global.apps.server.plugin;

import com.daou.dop.global.apps.domain.enums.AuthType;
import com.daou.dop.global.apps.domain.enums.PluginStatus;
import com.daou.dop.global.apps.domain.plugin.Plugin;
import com.daou.dop.global.apps.domain.plugin.PluginRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 플러그인 초기 데이터 설정
 * 애플리케이션 시작 시 plugin 테이블에 기본 플러그인 정보 등록
 */
@Component
public class PluginDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PluginDataInitializer.class);

    private final PluginRepository pluginRepository;
    private final Environment environment;
    private final ObjectMapper objectMapper;

    public PluginDataInitializer(
            PluginRepository pluginRepository,
            Environment environment,
            ObjectMapper objectMapper) {
        this.pluginRepository = pluginRepository;
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initSlackPlugin();
    }

    private void initSlackPlugin() {
        String pluginId = "slack";

        if (pluginRepository.existsByPluginId(pluginId)) {
            log.debug("Plugin already exists: {}", pluginId);
            return;
        }

        // 환경변수 또는 application.yml에서 설정 조회
        String clientId = environment.getProperty("slack.client-id", "");
        String clientSecret = environment.getProperty("slack.client-secret", "");
        String signingSecret = environment.getProperty("slack.signing-secret", "");
        String scopes = environment.getProperty("slack.scopes",
                "channels:read,channels:history,chat:write,chat:write.public,users:read");

        if (clientId.isBlank()) {
            log.warn("Slack client-id not configured. Plugin will be created but OAuth will not work.");
        }

        try {
            // secrets JSON
            String secretsJson = objectMapper.writeValueAsString(Map.of(
                    "signing_secret", signingSecret
            ));

            // metadata JSON
            String metadataJson = objectMapper.writeValueAsString(Map.of(
                    "scopes", scopes,
                    "authUrl", "https://slack.com/oauth/v2/authorize",
                    "tokenUrl", "https://slack.com/api/oauth.v2.access",
                    "apiBaseUrl", "https://slack.com/api"
            ));

            Plugin plugin = Plugin.builder()
                    .pluginId(pluginId)
                    .name("Slack")
                    .description("Slack 워크스페이스 연동")
                    .authType(AuthType.OAUTH2)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .secrets(secretsJson)
                    .metadata(metadataJson)
                    .iconUrl("https://a.slack-edge.com/80588/marketing/img/icons/icon_slack_hash_colored.png")
                    .status(PluginStatus.ACTIVE)
                    .build();

            pluginRepository.save(plugin);
            log.info("Initialized plugin: {}", pluginId);

        } catch (Exception e) {
            log.error("Failed to initialize plugin: {}", pluginId, e);
        }
    }
}
