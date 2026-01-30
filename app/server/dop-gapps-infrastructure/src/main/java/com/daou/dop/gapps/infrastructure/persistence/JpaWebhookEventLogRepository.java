package com.daou.dop.gapps.infrastructure.persistence;

import com.daou.dop.gapps.core.repository.WebhookEventLogRepository;
import com.daou.dop.gapps.domain.enums.WebhookEventStatus;
import com.daou.dop.gapps.domain.webhook.WebhookEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * WebhookEventLog JPA Repository
 */
@Repository
public interface JpaWebhookEventLogRepository extends JpaRepository<WebhookEventLog, Long>, WebhookEventLogRepository {

    List<WebhookEventLog> findByPluginId(String pluginId);

    List<WebhookEventLog> findByConnectionId(Long connectionId);

    List<WebhookEventLog> findByStatus(WebhookEventStatus status);

    List<WebhookEventLog> findByPluginIdAndStatus(String pluginId, WebhookEventStatus status);

    List<WebhookEventLog> findByPluginIdAndCreatedAtBetween(String pluginId, Instant start, Instant end);

    @Query("SELECT e FROM WebhookEventLog e WHERE e.pluginId = :pluginId ORDER BY e.createdAt DESC LIMIT :limit")
    List<WebhookEventLog> findRecentByPluginId(@Param("pluginId") String pluginId, @Param("limit") int limit);

    @Query("SELECT e FROM WebhookEventLog e WHERE e.connectionId = :connectionId ORDER BY e.createdAt DESC LIMIT :limit")
    List<WebhookEventLog> findRecentByConnectionId(@Param("connectionId") Long connectionId, @Param("limit") int limit);

    @Query("SELECT e FROM WebhookEventLog e WHERE e.pluginId = :pluginId AND e.connectionId = :connectionId ORDER BY e.createdAt DESC LIMIT :limit")
    List<WebhookEventLog> findRecentByPluginIdAndConnectionId(
            @Param("pluginId") String pluginId,
            @Param("connectionId") Long connectionId,
            @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM WebhookEventLog e WHERE e.createdAt < :before")
    int deleteByCreatedAtBefore(@Param("before") Instant before);
}
