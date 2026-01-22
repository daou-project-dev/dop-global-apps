package com.daou.dop.global.apps.api.connection.service;

import com.daou.dop.global.apps.api.connection.dto.ConnectionResponse;
import com.daou.dop.global.apps.core.repository.OAuthCredentialRepository;
import com.daou.dop.global.apps.core.repository.PluginConnectionRepository;
import com.daou.dop.global.apps.domain.connection.PluginConnection;
import com.daou.dop.global.apps.domain.credential.OAuthCredential;
import com.daou.dop.global.apps.domain.enums.ConnectionStatus;
import com.daou.dop.global.apps.domain.enums.ScopeType;
import com.daou.dop.global.apps.plugin.sdk.CredentialContext;
import com.daou.dop.global.apps.plugin.sdk.TokenInfo;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 플러그인 연동 관리 서비스
 */
@Service
@Transactional(readOnly = true)
public class ConnectionService {

    private static final Logger log = LoggerFactory.getLogger(ConnectionService.class);

    private final PluginConnectionRepository connectionRepository;
    private final OAuthCredentialRepository credentialRepository;
    private final ObjectMapper objectMapper;

    public ConnectionService(
            PluginConnectionRepository connectionRepository,
            OAuthCredentialRepository credentialRepository,
            ObjectMapper objectMapper) {
        this.connectionRepository = connectionRepository;
        this.credentialRepository = credentialRepository;
        this.objectMapper = objectMapper;
    }

    // ========== 연동 조회 ==========

    /**
     * ID로 연동 조회
     */
    public Optional<PluginConnection> findById(Long id) {
        return connectionRepository.findById(id);
    }

    /**
     * 플러그인 ID와 외부 ID로 연동 조회
     */
    public Optional<PluginConnection> findByPluginIdAndExternalId(String pluginId, String externalId) {
        return connectionRepository.findByPluginIdAndExternalId(pluginId, externalId);
    }

    /**
     * 플러그인별 활성 연동 목록 조회
     */
    public List<PluginConnection> findActiveByPluginId(String pluginId) {
        return connectionRepository.findByPluginIdAndStatus(pluginId, ConnectionStatus.ACTIVE);
    }

    /**
     * 고객사별 활성 연동 목록 조회
     */
    public List<PluginConnection> findActiveByCompanyId(Long companyId) {
        return connectionRepository.findByCompanyIdAndStatus(companyId, ConnectionStatus.ACTIVE);
    }

    /**
     * 활성 연동 목록 조회 (DTO)
     *
     * @param companyId 고객사 ID (nullable - null이면 companyId가 null인 연동만 조회)
     * @param userId    사용자 ID (nullable - 현재 미사용)
     * @return 연동 목록 DTO
     */
    public List<ConnectionResponse> getActiveConnections(Long companyId, Long userId) {
        return connectionRepository.findByCompanyIdAndStatus(companyId, ConnectionStatus.ACTIVE)
                .stream()
                .map(ConnectionResponse::from)
                .toList();
    }

    // ========== 연동 + 토큰 저장 ==========

    /**
     * OAuth 토큰 저장 (연동 생성 또는 업데이트)
     *
     * @param tokenInfo 토큰 정보 (플러그인에서 전달)
     * @param companyId 고객사 ID (nullable)
     * @param userId    사용자 ID (nullable, USER 타입일 때)
     * @param scopeType 연동 범위
     * @return 저장된 연동 정보
     */
    @Transactional
    public PluginConnection saveOAuthToken(TokenInfo tokenInfo, Long companyId, Long userId, ScopeType scopeType) {
        String pluginId = tokenInfo.pluginId();
        String externalId = tokenInfo.externalId();

        // 기존 연동 확인
        Optional<PluginConnection> existing = connectionRepository
                .findByPluginIdAndExternalId(pluginId, externalId);

        PluginConnection connection;
        if (existing.isPresent()) {
            // 기존 연동 업데이트
            connection = existing.get();
            connection.updateExternalInfo(externalId, tokenInfo.externalName());
            connection.activate();

            if (tokenInfo.metadata() != null) {
                try {
                    connection.updateMetadata(objectMapper.writeValueAsString(tokenInfo.metadata()));
                } catch (Exception e) {
                    log.warn("Failed to serialize metadata", e);
                }
            }

            connection = connectionRepository.save(connection);
            log.info("Updated connection: plugin={}, externalId={}", pluginId, externalId);

            // 인증 정보 업데이트
            updateCredential(connection.getId(), tokenInfo);
        } else {
            // 신규 연동 생성
            String metadataJson = null;
            if (tokenInfo.metadata() != null) {
                try {
                    metadataJson = objectMapper.writeValueAsString(tokenInfo.metadata());
                } catch (Exception e) {
                    log.warn("Failed to serialize metadata", e);
                }
            }

            connection = PluginConnection.builder()
                    .pluginId(pluginId)
                    .companyId(companyId)
                    .userId(userId)
                    .scopeType(scopeType)
                    .externalId(externalId)
                    .externalName(tokenInfo.externalName())
                    .metadata(metadataJson)
                    .status(ConnectionStatus.ACTIVE)
                    .build();

            connection = connectionRepository.save(connection);
            log.info("Created connection: plugin={}, externalId={}", pluginId, externalId);

            // 인증 정보 생성
            createCredential(connection.getId(), tokenInfo);
        }

        return connection;
    }

    /**
     * 간편 저장 (companyId, userId 없이 WORKSPACE 타입으로)
     */
    @Transactional
    public PluginConnection saveOAuthToken(TokenInfo tokenInfo) {
        return saveOAuthToken(tokenInfo, null, null, ScopeType.WORKSPACE);
    }

    // ========== 인증 정보 관리 ==========

    private void createCredential(Long connectionId, TokenInfo tokenInfo) {
        OAuthCredential credential = OAuthCredential.builder()
                .connectionId(connectionId)
                .accessToken(tokenInfo.accessToken())
                .refreshToken(tokenInfo.refreshToken())
                .scope(tokenInfo.scope())
                .expiresAt(tokenInfo.expiresAt())
                .build();

        credentialRepository.save(credential);
        log.debug("Created credential for connection: {}", connectionId);
    }

    private void updateCredential(Long connectionId, TokenInfo tokenInfo) {
        credentialRepository.findByConnectionId(connectionId)
                .ifPresentOrElse(
                        credential -> {
                            credential.updateToken(
                                    tokenInfo.accessToken(),
                                    tokenInfo.refreshToken(),
                                    tokenInfo.scope(),
                                    tokenInfo.expiresAt()
                            );
                            credentialRepository.save(credential);
                            log.debug("Updated credential for connection: {}", connectionId);
                        },
                        () -> createCredential(connectionId, tokenInfo)
                );
    }

    /**
     * 연동 ID로 인증 정보 조회
     */
    public Optional<OAuthCredential> findCredentialByConnectionId(Long connectionId) {
        return credentialRepository.findByConnectionId(connectionId);
    }

    /**
     * 연동 정보를 CredentialContext로 변환
     */
    public Optional<CredentialContext> getCredentialContext(Long connectionId) {
        return connectionRepository.findById(connectionId)
                .flatMap(connection -> credentialRepository.findByConnectionId(connectionId)
                        .map(credential -> toCredentialContext(connection, credential)));
    }

    /**
     * 플러그인 ID와 외부 ID로 CredentialContext 조회
     */
    public Optional<CredentialContext> getCredentialContext(String pluginId, String externalId) {
        return connectionRepository.findByPluginIdAndExternalId(pluginId, externalId)
                .flatMap(connection -> credentialRepository.findByConnectionId(connection.getId())
                        .map(credential -> toCredentialContext(connection, credential)));
    }

    private CredentialContext toCredentialContext(PluginConnection connection, OAuthCredential credential) {
        Map<String, String> metadata = parseMetadata(connection.getMetadata());

        return CredentialContext.builder()
                .accessToken(credential.getAccessToken())
                .refreshToken(credential.getRefreshToken())
                .expiresAt(credential.getExpiresAt())
                .externalId(connection.getExternalId())
                .metadata(metadata)
                .build();
    }

    private Map<String, String> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse metadata: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    // ========== 연동 해제 ==========

    /**
     * 연동 해제 (REVOKED 상태로 변경)
     */
    @Transactional
    public void revokeConnection(Long connectionId) {
        connectionRepository.findById(connectionId)
                .ifPresent(connection -> {
                    connection.revoke();
                    connectionRepository.save(connection);
                    log.info("Revoked connection: {}", connectionId);
                });
    }

    /**
     * 연동 삭제 (완전 삭제)
     */
    @Transactional
    public void deleteConnection(Long connectionId) {
        credentialRepository.deleteByConnectionId(connectionId);
        connectionRepository.deleteById(connectionId);
        log.info("Deleted connection: {}", connectionId);
    }
}
