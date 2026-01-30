package com.daou.dop.gapps.domain.webhook;

import com.daou.dop.gapps.domain.enums.WebhookEventStatus;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * 웹훅 이벤트 로그 Entity
 *
 * <p>웹훅 수신/처리 이력 관리
 */
@Entity
@Table(name = "webhook_event_log", indexes = {
        @Index(name = "idx_webhook_log_plugin", columnList = "pluginId, createdAt"),
        @Index(name = "idx_webhook_log_connection", columnList = "connectionId"),
        @Index(name = "idx_webhook_log_status", columnList = "status")
})
public class WebhookEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 플러그인 ID
     */
    @Column(nullable = false, length = 50)
    private String pluginId;

    /**
     * 연동 ID (식별된 경우)
     */
    @Column
    private Long connectionId;

    /**
     * 이벤트 타입
     */
    @Column(length = 100)
    private String eventType;

    /**
     * 외부 시스템 ID (team_id, cloudId 등)
     */
    @Column(length = 100)
    private String externalId;

    /**
     * 원본 페이로드
     */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /**
     * 처리 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookEventStatus status;

    /**
     * 에러 메시지 (실패 시)
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 처리 완료 시간
     */
    @Column
    private Instant processedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected WebhookEventLog() {
    }

    private WebhookEventLog(Builder builder) {
        this.pluginId = builder.pluginId;
        this.connectionId = builder.connectionId;
        this.eventType = builder.eventType;
        this.externalId = builder.externalId;
        this.payload = builder.payload;
        this.status = builder.status != null ? builder.status : WebhookEventStatus.RECEIVED;
        this.errorMessage = builder.errorMessage;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public String getPluginId() { return pluginId; }
    public Long getConnectionId() { return connectionId; }
    public String getEventType() { return eventType; }
    public String getExternalId() { return externalId; }
    public String getPayload() { return payload; }
    public WebhookEventStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getProcessedAt() { return processedAt; }
    public Instant getCreatedAt() { return createdAt; }

    // Update methods
    public void markSuccess() {
        this.status = WebhookEventStatus.SUCCESS;
        this.processedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = WebhookEventStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = Instant.now();
    }

    public void updateConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    public void updateEventType(String eventType) {
        this.eventType = eventType;
    }

    public static class Builder {
        private String pluginId;
        private Long connectionId;
        private String eventType;
        private String externalId;
        private String payload;
        private WebhookEventStatus status;
        private String errorMessage;

        public Builder pluginId(String pluginId) { this.pluginId = pluginId; return this; }
        public Builder connectionId(Long connectionId) { this.connectionId = connectionId; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder externalId(String externalId) { this.externalId = externalId; return this; }
        public Builder payload(String payload) { this.payload = payload; return this; }
        public Builder status(WebhookEventStatus status) { this.status = status; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public WebhookEventLog build() {
            return new WebhookEventLog(this);
        }
    }
}
