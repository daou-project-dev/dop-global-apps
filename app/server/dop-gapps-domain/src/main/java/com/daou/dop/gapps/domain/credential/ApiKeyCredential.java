package com.daou.dop.gapps.domain.credential;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * API Key 인증 정보 Entity
 *
 * <p>PluginConnection과 1:1 관계
 * <p>api_key, api_secret은 infrastructure에서 암호화 Converter 적용
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    // Update methods
    public void updateApiKey(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.updatedAt = Instant.now();
    }
}
