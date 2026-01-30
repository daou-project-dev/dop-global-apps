package com.daou.dop.global.apps.domain.credential;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * OAuth 인증 정보 Entity
 *
 * <p>PluginConnection과 1:1 관계
 * <p>access_token, refresh_token은 infrastructure에서 암호화 Converter 적용
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    @Builder
    private OAuthCredential(Long connectionId, String accessToken, String refreshToken,
                            String scope, Instant expiresAt) {
        this.connectionId = connectionId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

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
}
