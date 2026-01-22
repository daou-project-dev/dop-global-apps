package com.daou.dop.global.apps.domain.credential;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * OAuth 인증 정보 Entity
 *
 * <p>PluginConnection과 1:1 관계
 * <p>access_token, refresh_token은 infrastructure에서 암호화 Converter 적용
 */
@Entity
@Table(name = "oauth_credential", indexes = {
        @Index(name = "idx_oauth_credential_connection_id", columnList = "connectionId", unique = true)
})
public class OAuthCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연동 ID (plugin_connection.id 논리적 참조)
     */
    @Column(nullable = false)
    private Long connectionId;

    /**
     * 액세스 토큰 (암호화)
     */
    @Column(nullable = false, length = 1024)
    private String accessToken;

    /**
     * 리프레시 토큰 (암호화, nullable)
     */
    @Column(length = 1024)
    private String refreshToken;

    /**
     * 권한 범위
     */
    @Column(length = 1000)
    private String scope;

    /**
     * 토큰 만료 시간
     */
    @Column
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected OAuthCredential() {
    }

    private OAuthCredential(Builder builder) {
        this.connectionId = builder.connectionId;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.scope = builder.scope;
        this.expiresAt = builder.expiresAt;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public Long getConnectionId() { return connectionId; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getScope() { return scope; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    // Update methods
    public void updateToken(String accessToken, String refreshToken, String scope, Instant expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.updatedAt = Instant.now();
    }

    public void updateAccessToken(String accessToken, Instant expiresAt) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private Long connectionId;
        private String accessToken;
        private String refreshToken;
        private String scope;
        private Instant expiresAt;

        public Builder connectionId(Long connectionId) { this.connectionId = connectionId; return this; }
        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder scope(String scope) { this.scope = scope; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }

        public OAuthCredential build() {
            return new OAuthCredential(this);
        }
    }
}
