package com.daou.dop.global.apps.domain.webhook;

import com.daou.dop.global.apps.domain.enums.WebhookTargetType;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * 웹훅 구독 Entity
 *
 * <p>웹훅 이벤트를 어디로 디스패치할지 설정
 */
@Entity
@Table(name = "webhook_subscription", indexes = {
        @Index(name = "idx_webhook_sub_plugin_event", columnList = "pluginId, eventType"),
        @Index(name = "idx_webhook_sub_connection", columnList = "connectionId")
})
public class WebhookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 플러그인 ID (slack, jira 등)
     */
    @Column(nullable = false, length = 50)
    private String pluginId;

    /**
     * 이벤트 타입 (null이면 전체)
     */
    @Column(length = 100)
    private String eventType;

    /**
     * 특정 연동만 (null이면 전체)
     */
    @Column
    private Long connectionId;

    /**
     * 디스패치 대상 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookTargetType targetType;

    /**
     * HTTP 호출 URL
     */
    @Column(length = 500)
    private String targetUrl;

    /**
     * 내부 메서드 (ServiceName.methodName)
     */
    @Column(length = 100)
    private String targetMethod;

    /**
     * JSONPath 필터 표현식
     */
    @Column(columnDefinition = "TEXT")
    private String filterExpr;

    /**
     * 재시도 정책 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String retryPolicy;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected WebhookSubscription() {
    }

    private WebhookSubscription(Builder builder) {
        this.pluginId = builder.pluginId;
        this.eventType = builder.eventType;
        this.connectionId = builder.connectionId;
        this.targetType = builder.targetType;
        this.targetUrl = builder.targetUrl;
        this.targetMethod = builder.targetMethod;
        this.filterExpr = builder.filterExpr;
        this.retryPolicy = builder.retryPolicy;
        this.enabled = builder.enabled;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public String getPluginId() { return pluginId; }
    public String getEventType() { return eventType; }
    public Long getConnectionId() { return connectionId; }
    public WebhookTargetType getTargetType() { return targetType; }
    public String getTargetUrl() { return targetUrl; }
    public String getTargetMethod() { return targetMethod; }
    public String getFilterExpr() { return filterExpr; }
    public String getRetryPolicy() { return retryPolicy; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Update methods
    public void updateTarget(WebhookTargetType targetType, String targetUrl, String targetMethod) {
        this.targetType = targetType;
        this.targetUrl = targetUrl;
        this.targetMethod = targetMethod;
        this.updatedAt = Instant.now();
    }

    public void updateFilter(String filterExpr) {
        this.filterExpr = filterExpr;
        this.updatedAt = Instant.now();
    }

    public void updateRetryPolicy(String retryPolicy) {
        this.retryPolicy = retryPolicy;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private String pluginId;
        private String eventType;
        private Long connectionId;
        private WebhookTargetType targetType;
        private String targetUrl;
        private String targetMethod;
        private String filterExpr;
        private String retryPolicy;
        private boolean enabled = true;

        public Builder pluginId(String pluginId) { this.pluginId = pluginId; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder connectionId(Long connectionId) { this.connectionId = connectionId; return this; }
        public Builder targetType(WebhookTargetType targetType) { this.targetType = targetType; return this; }
        public Builder targetUrl(String targetUrl) { this.targetUrl = targetUrl; return this; }
        public Builder targetMethod(String targetMethod) { this.targetMethod = targetMethod; return this; }
        public Builder filterExpr(String filterExpr) { this.filterExpr = filterExpr; return this; }
        public Builder retryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }

        public WebhookSubscription build() {
            return new WebhookSubscription(this);
        }
    }
}
