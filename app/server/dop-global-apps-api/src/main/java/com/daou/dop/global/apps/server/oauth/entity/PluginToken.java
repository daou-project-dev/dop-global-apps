package com.daou.dop.global.apps.server.oauth.entity;

import com.daou.dop.global.apps.core.crypto.EncryptedStringConverter;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 플러그인 토큰 Entity
 */
@Entity
@Table(name = "plugin_token", indexes = {
        @Index(name = "idx_plugin_token_plugin_external", columnList = "pluginId, externalId", unique = true)
})
public class PluginToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pluginId;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String externalName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 1024)
    private String accessToken;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 1024)
    private String refreshToken;

    @Column(length = 1000)
    private String scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status = TokenStatus.ACTIVE;

    @Column
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant installedAt;

    @Column
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plugin_token_metadata", joinColumns = @JoinColumn(name = "token_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value", length = 1000)
    private Map<String, String> metadata = new HashMap<>();

    protected PluginToken() {
    }

    private PluginToken(Builder builder) {
        this.pluginId = builder.pluginId;
        this.externalId = builder.externalId;
        this.externalName = builder.externalName;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.scope = builder.scope;
        this.status = builder.status != null ? builder.status : TokenStatus.ACTIVE;
        this.expiresAt = builder.expiresAt;
        this.installedAt = builder.installedAt != null ? builder.installedAt : Instant.now();
        this.metadata = builder.metadata != null ? builder.metadata : new HashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getExternalName() {
        return externalName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getInstalledAt() {
        return installedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    // Update methods
    public void updateToken(String accessToken, String refreshToken, String scope, Instant expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.updatedAt = Instant.now();
    }

    public void revoke() {
        this.status = TokenStatus.REVOKED;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private String pluginId;
        private String externalId;
        private String externalName;
        private String accessToken;
        private String refreshToken;
        private String scope;
        private TokenStatus status;
        private Instant expiresAt;
        private Instant installedAt;
        private Map<String, String> metadata;

        public Builder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder externalName(String externalName) {
            this.externalName = externalName;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder status(TokenStatus status) {
            this.status = status;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder installedAt(Instant installedAt) {
            this.installedAt = installedAt;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public PluginToken build() {
            return new PluginToken(this);
        }
    }
}
