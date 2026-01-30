package com.daou.dop.gapps.core.repository;

import com.daou.dop.gapps.domain.enums.WebhookEventStatus;
import com.daou.dop.gapps.domain.webhook.WebhookEventLog;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * WebhookEventLog Repository Port
 */
public interface WebhookEventLogRepository {

    Optional<WebhookEventLog> findById(Long id);

    List<WebhookEventLog> findByPluginId(String pluginId);

    List<WebhookEventLog> findByConnectionId(Long connectionId);

    List<WebhookEventLog> findByStatus(WebhookEventStatus status);

    List<WebhookEventLog> findByPluginIdAndStatus(String pluginId, WebhookEventStatus status);

    List<WebhookEventLog> findByPluginIdAndCreatedAtBetween(String pluginId, Instant start, Instant end);

    /**
     * 최신 로그 조회 (페이징)
     */
    List<WebhookEventLog> findRecentByPluginId(String pluginId, int limit);

    List<WebhookEventLog> findRecentByConnectionId(Long connectionId, int limit);

    List<WebhookEventLog> findRecentByPluginIdAndConnectionId(String pluginId, Long connectionId, int limit);

    WebhookEventLog save(WebhookEventLog eventLog);

    void deleteById(Long id);

    /**
     * 오래된 로그 삭제 (배치용)
     *
     * @param before 이 시간 이전 로그 삭제
     * @return 삭제된 건수
     */
    int deleteByCreatedAtBefore(Instant before);
}
