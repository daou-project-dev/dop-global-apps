package com.daou.dop.global.apps.domain.plugin;

import com.daou.dop.global.apps.domain.enums.AuthType;
import com.daou.dop.global.apps.domain.enums.PluginStatus;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * 플러그인 마스터 Entity
 *
 * <p>OAuth client credentials 및 플러그인 설정 관리
 */
@Entity
@Table(name = "plugin", indexes = {
        @Index(name = "idx_plugin_plugin_id", columnList = "pluginId", unique = true)
})
public class Plugin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 플러그인 고유 식별자 ("slack", "google" 등)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String pluginId;

    /**
     * 플러그인 표시명
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 플러그인 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 인증 방식
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthType authType;

    /**
     * OAuth Client ID
     */
    @Column(length = 200)
    private String clientId;

    /**
     * OAuth Client Secret (암호화 - infrastructure에서 Converter 적용)
     */
    @Column(length = 500)
    private String clientSecret;

    /**
     * 플러그인별 추가 민감 정보 (JSON, 암호화)
     */
    @Column(columnDefinition = "TEXT")
    private String secrets;

    /**
     * OAuth URL, scopes 등 일반 설정 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * 아이콘 URL
     */
    @Column(length = 500)
    private String iconUrl;

    /**
     * 플러그인 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PluginStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected Plugin() {
    }

    private Plugin(Builder builder) {
        this.pluginId = builder.pluginId;
        this.name = builder.name;
        this.description = builder.description;
        this.authType = builder.authType;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.secrets = builder.secrets;
        this.metadata = builder.metadata;
        this.iconUrl = builder.iconUrl;
        this.status = builder.status != null ? builder.status : PluginStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public String getPluginId() { return pluginId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AuthType getAuthType() { return authType; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getSecrets() { return secrets; }
    public String getMetadata() { return metadata; }
    public String getIconUrl() { return iconUrl; }
    public PluginStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isActive() {
        return status == PluginStatus.ACTIVE;
    }

    // Update methods
    public void updateCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.updatedAt = Instant.now();
    }

    public void updateSecrets(String secrets) {
        this.secrets = secrets;
        this.updatedAt = Instant.now();
    }

    public void updateMetadata(String metadata) {
        this.metadata = metadata;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = PluginStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = PluginStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private String pluginId;
        private String name;
        private String description;
        private AuthType authType;
        private String clientId;
        private String clientSecret;
        private String secrets;
        private String metadata;
        private String iconUrl;
        private PluginStatus status;

        public Builder pluginId(String pluginId) { this.pluginId = pluginId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder authType(AuthType authType) { this.authType = authType; return this; }
        public Builder clientId(String clientId) { this.clientId = clientId; return this; }
        public Builder clientSecret(String clientSecret) { this.clientSecret = clientSecret; return this; }
        public Builder secrets(String secrets) { this.secrets = secrets; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder iconUrl(String iconUrl) { this.iconUrl = iconUrl; return this; }
        public Builder status(PluginStatus status) { this.status = status; return this; }

        public Plugin build() {
            return new Plugin(this);
        }
    }
}
