package com.daou.dop.gapps.api.webhook.controller;

import com.daou.dop.gapps.core.webhook.WebhookResult;
import com.daou.dop.gapps.core.webhook.WebhookService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 웹훅 수신 Controller
 * 외부 서비스(Slack, Jira, GitHub 등)에서 보내는 웹훅 수신
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * 웹훅 수신 (pluginId로만 식별)
     *
     * POST /webhook/{pluginId}
     */
    @PostMapping("/{pluginId}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String pluginId,
            @RequestBody byte[] payload,
            @RequestHeader Map<String, String> headers) {

        WebhookResult result = webhookService.handleWebhook(pluginId, payload, normalizeHeaders(headers));

        return buildResponse(result);
    }

    // @TODO: connectionId UUID 방식으로 변경
    /**
     * 웹훅 수신 (connectionId 명시)
     *
     * POST /webhook/{pluginId}/{connectionId}
     */
    @PostMapping("/{pluginId}/{connectionId}")
    public ResponseEntity<String> handleWebhookWithConnection(
            @PathVariable String pluginId,
            @PathVariable Long connectionId,
            @RequestBody byte[] payload,
            @RequestHeader Map<String, String> headers) {

        WebhookResult result = webhookService.handleWebhook(pluginId, connectionId, payload, normalizeHeaders(headers));

        return buildResponse(result);
    }

    private ResponseEntity<String> buildResponse(WebhookResult result) {
        MediaType contentType = result.contentType() != null
                ? MediaType.parseMediaType(result.contentType())
                : MediaType.APPLICATION_JSON;

        return ResponseEntity
                .status(result.statusCode())
                .contentType(contentType)
                .body(result.body());
    }

    /**
     * 헤더 키를 소문자로 정규화
     */
    private Map<String, String> normalizeHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toLowerCase(),
                        Map.Entry::getValue,
                        (v1, v2) -> v1
                ));
    }
}
