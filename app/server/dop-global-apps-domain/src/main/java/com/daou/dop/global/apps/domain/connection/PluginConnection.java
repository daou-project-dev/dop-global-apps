package com.daou.dop.global.apps.domain.connection;

import com.daou.dop.global.apps.domain.enums.ConnectionStatus;
import com.daou.dop.global.apps.domain.enums.ScopeType;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * 플러그인 연동 정보 Entity
 *
 * <p>고객사/사용자와 외부 서비스 연동 관리
 * <p>모든 참조는 논리적 (FK 제약 없음)
 */
@Entity
@Table(name = "plugin_connection", indexes = {
        @Index(name = "idx_connection_plugin_external", columnList = "pluginId, externalId"),
        @Index(name = "idx_connection_company", columnList = "companyId"),
        @Index(name = "idx_connection_user", columnList = "userId")
})
public class PluginConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 플러그인 ID (논리적 참조)
     */
    @Column(nullable = false, length = 50)
    private String pluginId;

    /**
     * 고객사 ID (논리적 참조, nullable)
     */
    @Column
    private Long companyId;

    /**
     * 사용자 ID (USER 타입일 때, 논리적 참조)
     */
    @Column
    private Long userId;

    /**
     * 연동 범위 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScopeType scopeType;

    /**
     * 외부 시스템 ID (Slack Team ID 등)
     */
    @Column(length = 100)
    private String externalId;

    /**
     * 표시용 이름 (Slack Workspace Name 등)
     */
    @Column(length = 200)
    private String externalName;

    /**
     * 연동별 추가 정보 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * 연동 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected PluginConnection() {
    }

    private PluginConnection(Builder builder) {
        this.pluginId = builder.pluginId;
        this.companyId = builder.companyId;
        this.userId = builder.userId;
        this.scopeType = builder.scopeType != null ? builder.scopeType : ScopeType.WORKSPACE;
        this.externalId = builder.externalId;
        this.externalName = builder.externalName;
        this.metadata = builder.metadata;
        this.status = builder.status != null ? builder.status : ConnectionStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public String getPluginId() { return pluginId; }
    public Long getCompanyId() { return companyId; }
    public Long getUserId() { return userId; }
    public ScopeType getScopeType() { return scopeType; }
    public String getExternalId() { return externalId; }
    public String getExternalName() { return externalName; }
    public String getMetadata() { return metadata; }
    public ConnectionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isActive() {
        return status == ConnectionStatus.ACTIVE;
    }

    // Update methods
    public void updateExternalInfo(String externalId, String externalName) {
        this.externalId = externalId;
        this.externalName = externalName;
        this.updatedAt = Instant.now();
    }

    public void updateMetadata(String metadata) {
        this.metadata = metadata;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = ConnectionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void revoke() {
        this.status = ConnectionStatus.REVOKED;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private String pluginId;
        private Long companyId;
        private Long userId;
        private ScopeType scopeType;
        private String externalId;
        private String externalName;
        private String metadata;
        private ConnectionStatus status;

        public Builder pluginId(String pluginId) { this.pluginId = pluginId; return this; }
        public Builder companyId(Long companyId) { this.companyId = companyId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder scopeType(ScopeType scopeType) { this.scopeType = scopeType; return this; }
        public Builder externalId(String externalId) { this.externalId = externalId; return this; }
        public Builder externalName(String externalName) { this.externalName = externalName; return this; }
        public Builder metadata(String metadata) { this.metadata = metadata; return this; }
        public Builder status(ConnectionStatus status) { this.status = status; return this; }

        public PluginConnection build() {
            return new PluginConnection(this);
        }
    }
}
