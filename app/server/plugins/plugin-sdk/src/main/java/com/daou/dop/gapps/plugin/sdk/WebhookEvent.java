package com.daou.dop.gapps.plugin.sdk;

import java.time.Instant;
import java.util.Map;

/**
 * 파싱된 웹훅 이벤트
 *
 * @param pluginId       플러그인 ID (slack, jira, github 등)
 * @param eventType      이벤트 타입 (message, issue_created, push 등)
 * @param externalId     외부 시스템 ID (teamId, cloudId, repositoryId 등)
 * @param externalUserId 이벤트 발생 사용자 (nullable)
 * @param timestamp      이벤트 발생 시간
 * @param data           이벤트 상세 데이터
 * @param connectionId   연동 ID (서버에서 enrichment)
 * @param companyId      고객사 ID (서버에서 enrichment)
 */
public record WebhookEvent(
        String pluginId,
        String eventType,
        String externalId,
        String externalUserId,
        Instant timestamp,
        Map<String, Object> data,
        Long connectionId,
        Long companyId
) {
    /**
     * 기본 생성자 (connectionId, companyId 없이)
     */
    public WebhookEvent(
            String pluginId,
            String eventType,
            String externalId,
            String externalUserId,
            Instant timestamp,
            Map<String, Object> data
    ) {
        this(pluginId, eventType, externalId, externalUserId, timestamp, data, null, null);
    }

    /**
     * Connection 정보 추가 (서버에서 사용)
     */
    public WebhookEvent withConnection(Long connectionId, Long companyId) {
        return new WebhookEvent(
                pluginId, eventType, externalId, externalUserId,
                timestamp, data, connectionId, companyId
        );
    }

    /**
     * 처리 대상 이벤트인지 확인
     * url_verification, ping 등은 처리 대상 아님
     */
    public boolean isProcessable() {
        return eventType != null
                && !eventType.equals("url_verification")
                && !eventType.equals("ping")
                && !eventType.equals("endpoint.url_validation");
    }
}
