package com.daou.dop.gapps.core.webhook;

import com.daou.dop.gapps.core.dto.PluginConfigInfo;
import com.daou.dop.gapps.core.plugin.PluginRegistry;
import com.daou.dop.gapps.core.plugin.PluginService;
import com.daou.dop.gapps.core.repository.PluginConnectionRepository;
import com.daou.dop.gapps.core.repository.WebhookEventLogRepository;
import com.daou.dop.gapps.domain.connection.PluginConnection;
import com.daou.dop.gapps.domain.enums.WebhookEventStatus;
import com.daou.dop.gapps.domain.webhook.WebhookEventLog;
import com.daou.dop.gapps.plugin.sdk.PluginConfig;
import com.daou.dop.gapps.plugin.sdk.WebhookEvent;
import com.daou.dop.gapps.plugin.sdk.WebhookHandler;
import com.daou.dop.gapps.plugin.sdk.WebhookImmediateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * 웹훅 수신 처리 서비스
 */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final PluginRegistry pluginRegistry;
    private final PluginService pluginService;
    private final PluginConnectionRepository connectionRepository;
    private final WebhookEventLogRepository eventLogRepository;
    private final WebhookDispatcher dispatcher;

    public WebhookService(
            PluginRegistry pluginRegistry,
            PluginService pluginService,
            PluginConnectionRepository connectionRepository,
            WebhookEventLogRepository eventLogRepository,
            WebhookDispatcher dispatcher) {
        this.pluginRegistry = pluginRegistry;
        this.pluginService = pluginService;
        this.connectionRepository = connectionRepository;
        this.eventLogRepository = eventLogRepository;
        this.dispatcher = dispatcher;
    }

    /**
     * 웹훅 처리 (connectionId 없이)
     */
    @Transactional
    public WebhookResult handleWebhook(String pluginId, byte[] payload, Map<String, String> headers) {
        return handleWebhook(pluginId, null, payload, headers);
    }

    /**
     * 웹훅 처리 (connectionId 포함)
     */
    @Transactional
    public WebhookResult handleWebhook(
            String pluginId,
            Long connectionId,
            byte[] payload,
            Map<String, String> headers) {

        String rawPayload = new String(payload, StandardCharsets.UTF_8);
        log.info("Webhook received: plugin={}, size={}", pluginId, payload.length);

        // 1. WebhookHandler 조회
        WebhookHandler handler = pluginRegistry.findWebhookHandler(pluginId)
                .orElse(null);

        if (handler == null) {
            log.warn("WebhookHandler not found: {}", pluginId);
            return WebhookResult.notFound("WebhookHandler not found: " + pluginId);
        }

        // 2. 로그 생성 (RECEIVED)
        WebhookEventLog eventLog = createEventLog(pluginId, rawPayload);

        try {
            // 3. 플러그인 설정 로드
            PluginConfigInfo configInfo = pluginService.getPluginConfig(pluginId)
                    .orElse(null);

            if (configInfo == null) {
                return handleError(eventLog, "Plugin config not found: " + pluginId);
            }

            PluginConfig config = toPluginConfig(configInfo);

            // 4. 서명 검증
            if (handler.supportsSignatureVerification()) {
                if (!handler.verifySignature(config, payload, headers)) {
                    log.warn("Webhook signature verification failed: plugin={}", pluginId);
                    eventLog.markFailed("Signature verification failed");
                    eventLogRepository.save(eventLog);
                    return WebhookResult.forbidden("Invalid signature");
                }
            }

            // 5. Connection 조회
            PluginConnection connection = resolveConnection(pluginId, connectionId, handler, rawPayload, headers);
            if (connection != null) {
                eventLog.updateConnectionId(connection.getId());
            }

            // 6. 이벤트 파싱
            WebhookEvent event = handler.parseEvent(rawPayload, headers);
            eventLog.updateEventType(event.eventType());

            // Connection 정보 enrichment
            if (connection != null) {
                event = event.withConnection(connection.getId(), connection.getCompanyId());
            }

            // 7. 즉시 응답 확인
            Optional<WebhookImmediateResponse> immediateResponse =
                    handler.getImmediateResponse(event, rawPayload);

            if (immediateResponse.isPresent()) {
                // 즉시 응답 필요한 경우
                if (event.isProcessable() && connection != null) {
                    dispatcher.dispatch(event);
                }
                eventLog.markSuccess();
                eventLogRepository.save(eventLog);
                log.info("Webhook processed with immediate response: plugin={}, event={}",
                        pluginId, event.eventType());
                return WebhookResult.of(immediateResponse.get());
            }

            // 8. 디스패치
            if (event.isProcessable() && connection != null) {
                dispatcher.dispatch(event);
            }

            eventLog.markSuccess();
            eventLogRepository.save(eventLog);
            log.info("Webhook processed: plugin={}, event={}", pluginId, event.eventType());

            return WebhookResult.ok();

        } catch (Exception e) {
            log.error("Webhook processing error: plugin={}", pluginId, e);
            return handleError(eventLog, e.getMessage());
        }
    }

    private WebhookEventLog createEventLog(String pluginId, String payload) {
        WebhookEventLog log = WebhookEventLog.builder()
                .pluginId(pluginId)
                .payload(payload)
                .status(WebhookEventStatus.RECEIVED)
                .build();
        return eventLogRepository.save(log);
    }

    private WebhookResult handleError(WebhookEventLog eventLog, String message) {
        eventLog.markFailed(message);
        eventLogRepository.save(eventLog);
        return WebhookResult.error(message);
    }

    private PluginConnection resolveConnection(
            String pluginId,
            Long connectionId,
            WebhookHandler handler,
            String rawPayload,
            Map<String, String> headers) {

        // URL에 connectionId가 있으면 바로 조회
        if (connectionId != null) {
            return connectionRepository.findById(connectionId).orElse(null);
        }

        // 페이로드에서 externalId 추출
        String externalId = handler.extractExternalId(rawPayload, headers);
        if (externalId == null || externalId.isBlank()) {
            return null;
        }

        return connectionRepository.findByPluginIdAndExternalId(pluginId, externalId).orElse(null);
    }

    private PluginConfig toPluginConfig(PluginConfigInfo info) {
        return PluginConfig.builder()
                .pluginId(info.pluginId())
                .clientId(info.clientId())
                .clientSecret(info.clientSecret())
                .secrets(info.secrets())
                .metadata(info.metadata())
                .build();
    }
}
