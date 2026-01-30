package com.daou.dop.gapps.core.repository;

import com.daou.dop.gapps.domain.webhook.WebhookSubscription;

import java.util.List;
import java.util.Optional;

/**
 * WebhookSubscription Repository Port
 */
public interface WebhookSubscriptionRepository {

    Optional<WebhookSubscription> findById(Long id);

    List<WebhookSubscription> findByPluginId(String pluginId);

    List<WebhookSubscription> findByPluginIdAndEnabled(String pluginId, boolean enabled);

    List<WebhookSubscription> findByPluginIdAndEventTypeAndEnabled(String pluginId, String eventType, boolean enabled);

    List<WebhookSubscription> findByConnectionId(Long connectionId);

    /**
     * 매칭되는 구독 조회 (디스패치용)
     *
     * @param pluginId 플러그인 ID
     * @param eventType 이벤트 타입
     * @param connectionId 연동 ID
     * @return 매칭되는 구독 목록
     */
    List<WebhookSubscription> findMatchingSubscriptions(String pluginId, String eventType, Long connectionId);

    WebhookSubscription save(WebhookSubscription subscription);

    void deleteById(Long id);
}
