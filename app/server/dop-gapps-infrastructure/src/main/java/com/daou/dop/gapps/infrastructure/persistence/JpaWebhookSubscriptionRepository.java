package com.daou.dop.gapps.infrastructure.persistence;

import com.daou.dop.gapps.core.repository.WebhookSubscriptionRepository;
import com.daou.dop.gapps.domain.webhook.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WebhookSubscription JPA Repository
 */
@Repository
public interface JpaWebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long>, WebhookSubscriptionRepository {

    List<WebhookSubscription> findByPluginId(String pluginId);

    List<WebhookSubscription> findByPluginIdAndEnabled(String pluginId, boolean enabled);

    List<WebhookSubscription> findByPluginIdAndEventTypeAndEnabled(String pluginId, String eventType, boolean enabled);

    List<WebhookSubscription> findByConnectionId(Long connectionId);

    /**
     * 매칭되는 구독 조회
     * - pluginId 일치
     * - eventType이 null이거나 일치
     * - connectionId가 null이거나 일치
     * - enabled = true
     */
    @Query("""
        SELECT s FROM WebhookSubscription s
        WHERE s.pluginId = :pluginId
          AND s.enabled = true
          AND (s.eventType IS NULL OR s.eventType = :eventType)
          AND (s.connectionId IS NULL OR s.connectionId = :connectionId)
        """)
    List<WebhookSubscription> findMatchingSubscriptions(
            @Param("pluginId") String pluginId,
            @Param("eventType") String eventType,
            @Param("connectionId") Long connectionId);
}
