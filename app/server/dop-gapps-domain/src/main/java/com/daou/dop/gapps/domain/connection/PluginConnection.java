package com.daou.dop.gapps.domain.connection;

import com.daou.dop.gapps.domain.enums.ConnectionStatus;
import com.daou.dop.gapps.domain.enums.ScopeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 플러그인 연동 정보 Entity
 *
 * <p>고객사/사용자와 외부 서비스 연동 관리
 * <p>모든 참조는 논리적 (FK 제약 없음)
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    @Builder
    private PluginConnection(
        String pluginId, Long companyId, Long userId, ScopeType scopeType,
        String externalId, String externalName, String metadata, ConnectionStatus status
    ) {
        this.pluginId = pluginId;
        this.companyId = companyId;
        this.userId = userId;
        this.scopeType = scopeType != null ? scopeType : ScopeType.WORKSPACE;
        this.externalId = externalId;
        this.externalName = externalName;
        this.metadata = metadata;
        this.status = status != null ? status : ConnectionStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

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
}
