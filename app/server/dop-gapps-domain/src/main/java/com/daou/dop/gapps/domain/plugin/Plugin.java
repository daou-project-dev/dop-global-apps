package com.daou.dop.gapps.domain.plugin;

import com.daou.dop.gapps.domain.enums.AuthType;
import com.daou.dop.gapps.domain.enums.PluginStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 플러그인 마스터 Entity
 *
 * <p>OAuth client credentials 및 플러그인 설정 관리
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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
}
