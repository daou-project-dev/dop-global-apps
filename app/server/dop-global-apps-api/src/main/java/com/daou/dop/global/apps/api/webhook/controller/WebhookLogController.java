package com.daou.dop.global.apps.api.webhook.controller;

import com.daou.dop.global.apps.core.repository.WebhookEventLogRepository;
import com.daou.dop.global.apps.domain.webhook.WebhookEventLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 웹훅 로그 조회 Controller
 */
@RestController
@RequestMapping("/api/webhook/logs")
public class WebhookLogController {

    private final WebhookEventLogRepository eventLogRepository;

    public WebhookLogController(WebhookEventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    /**
     * 플러그인별 최근 로그 조회
     *
     * GET /api/webhook/logs?pluginId=jira&limit=20
     */
    @GetMapping
    public ResponseEntity<List<WebhookEventLog>> getLogs(
            @RequestParam String pluginId,
            @RequestParam(required = false) Long connectionId,
            @RequestParam(defaultValue = "20") int limit) {

        List<WebhookEventLog> logs;

        if (connectionId != null) {
            logs = eventLogRepository.findRecentByPluginIdAndConnectionId(pluginId, connectionId, limit);
        } else {
            logs = eventLogRepository.findRecentByPluginId(pluginId, limit);
        }

        return ResponseEntity.ok(logs);
    }

    /**
     * 로그 상세 조회
     *
     * GET /api/webhook/logs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebhookEventLog> getLog(@PathVariable Long id) {
        return eventLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
