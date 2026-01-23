package com.daou.dop.global.apps.domain.credential;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * API Key 인증 정보 Entity
 *
 * <p>PluginConnection과 1:1 관계
 * <p>api_key, api_secret은 infrastructure에서 암호화 Converter 적용
 */
@Entity
@Table(name = "apikey_credential", indexes = {
        @Index(name = "idx_apikey_credential_connection_id", columnList = "connectionId", unique = true)
})
public class ApiKeyCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연동 ID (plugin_connection.id 논리적 참조)
     */
    @Column(nullable = false)
    private Long connectionId;

    /**
     * API Key (암호화)
     */
    @Column(nullable = false, length = 512)
    private String apiKey;

    /**
     * API Secret (암호화, nullable)
     */
    @Column(length = 512)
    private String apiSecret;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected ApiKeyCredential() {
    }

    private ApiKeyCredential(Builder builder) {
        this.connectionId = builder.connectionId;
        this.apiKey = builder.apiKey;
        this.apiSecret = builder.apiSecret;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public Long getConnectionId() { return connectionId; }
    public String getApiKey() { return apiKey; }
    public String getApiSecret() { return apiSecret; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Update methods
    public void updateApiKey(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private Long connectionId;
        private String apiKey;
        private String apiSecret;

        public Builder connectionId(Long connectionId) { this.connectionId = connectionId; return this; }
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder apiSecret(String apiSecret) { this.apiSecret = apiSecret; return this; }

        public ApiKeyCredential build() {
            return new ApiKeyCredential(this);
        }
    }
}
