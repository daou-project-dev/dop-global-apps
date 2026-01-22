package com.daou.dop.global.apps.core.connection;

import com.daou.dop.global.apps.core.credential.CredentialProvider;
import com.daou.dop.global.apps.core.dto.ConnectionInfo;
import com.daou.dop.global.apps.core.dto.CredentialInfo;
import com.daou.dop.global.apps.core.dto.OAuthTokenInfo;
import com.daou.dop.global.apps.core.enums.ScopeType;
import com.daou.dop.global.apps.core.repository.OAuthCredentialRepository;
import com.daou.dop.global.apps.core.repository.PluginConnectionRepository;
import com.daou.dop.global.apps.domain.connection.PluginConnection;
import com.daou.dop.global.apps.domain.credential.OAuthCredential;
import com.daou.dop.global.apps.domain.enums.ConnectionStatus;
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
public class ConnectionService implements CredentialProvider {

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

    // ========== CredentialProvider 구현 ==========

    @Override
    public Optional<CredentialInfo> getCredentialInfo(String pluginId, String externalId) {
        return connectionRepository.findByPluginIdAndExternalId(pluginId, externalId)
                .flatMap(connection -> credentialRepository.findByConnectionId(connection.getId())
                        .map(credential -> toCredentialInfo(connection, credential)));
    }

    // ========== 연동 조회 ==========

    public Optional<ConnectionInfo> findById(Long id) {
        return connectionRepository.findById(id)
                .map(this::toConnectionInfo);
    }

    public Optional<ConnectionInfo> findByPluginIdAndExternalId(String pluginId, String externalId) {
        return connectionRepository.findByPluginIdAndExternalId(pluginId, externalId)
                .map(this::toConnectionInfo);
    }

    public List<ConnectionInfo> findActiveByPluginId(String pluginId) {
        return connectionRepository.findByPluginIdAndStatus(pluginId, ConnectionStatus.ACTIVE)
                .stream()
                .map(this::toConnectionInfo)
                .toList();
    }

    public List<ConnectionInfo> findActiveByCompanyId(Long companyId) {
        return connectionRepository.findByCompanyIdAndStatus(companyId, ConnectionStatus.ACTIVE)
                .stream()
                .map(this::toConnectionInfo)
                .toList();
    }

    /**
     * 활성 연동 목록 조회
     *
     * @param companyId 고객사 ID (nullable - null이면 companyId가 null인 연동만 조회)
     * @param userId    사용자 ID (nullable - 현재 미사용)
     * @return 연동 목록
     */
    public List<ConnectionInfo> getActiveConnections(Long companyId, Long userId) {
        List<PluginConnection> connections = (companyId == null)
                ? connectionRepository.findByCompanyIdIsNullAndStatus(ConnectionStatus.ACTIVE)
                : connectionRepository.findByCompanyIdAndStatus(companyId, ConnectionStatus.ACTIVE);
        return connections.stream()
                .map(this::toConnectionInfo)
                .toList();
    }

    /**
     * 모든 활성 연동 목록 조회
     */
    public List<ConnectionInfo> findAllActive() {
        return connectionRepository.findByStatus(ConnectionStatus.ACTIVE)
                .stream()
                .map(this::toConnectionInfo)
                .toList();
    }

    // ========== 연동 + 토큰 저장 ==========

    /**
     * OAuth 토큰 저장 (연동 생성 또는 업데이트)
     * @return 저장된 연동 ID
     */
    @Transactional
    public Long saveOAuthToken(OAuthTokenInfo tokenInfo, Long companyId, Long userId, ScopeType scopeType) {
        String pluginId = tokenInfo.pluginId();
        String externalId = tokenInfo.externalId();

        Optional<PluginConnection> existing = connectionRepository
                .findByPluginIdAndExternalId(pluginId, externalId);

        PluginConnection connection;
        if (existing.isPresent()) {
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

            updateCredential(connection.getId(), tokenInfo);
        } else {
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
                    .scopeType(toDomainScopeType(scopeType))
                    .externalId(externalId)
                    .externalName(tokenInfo.externalName())
                    .metadata(metadataJson)
                    .status(ConnectionStatus.ACTIVE)
                    .build();

            connection = connectionRepository.save(connection);
            log.info("Created connection: plugin={}, externalId={}", pluginId, externalId);

            createCredential(connection.getId(), tokenInfo);
        }

        return connection.getId();
    }

    /**
     * 간편 저장 (companyId, userId 없이 WORKSPACE 타입으로)
     * @return 저장된 연동 ID
     */
    @Transactional
    public Long saveOAuthToken(OAuthTokenInfo tokenInfo) {
        return saveOAuthToken(tokenInfo, null, null, ScopeType.WORKSPACE);
    }

    // ========== 인증 정보 관리 ==========

    private void createCredential(Long connectionId, OAuthTokenInfo tokenInfo) {
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

    private void updateCredential(Long connectionId, OAuthTokenInfo tokenInfo) {
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

    // ========== 연동 해제 ==========

    @Transactional
    public void revokeConnection(Long connectionId) {
        connectionRepository.findById(connectionId)
                .ifPresent(connection -> {
                    connection.revoke();
                    connectionRepository.save(connection);
                    log.info("Revoked connection: {}", connectionId);
                });
    }

    @Transactional
    public void deleteConnection(Long connectionId) {
        credentialRepository.deleteByConnectionId(connectionId);
        connectionRepository.deleteById(connectionId);
        log.info("Deleted connection: {}", connectionId);
    }

    // ========== 변환 메서드 ==========

    private ConnectionInfo toConnectionInfo(PluginConnection connection) {
        return ConnectionInfo.builder()
                .id(connection.getId())
                .pluginId(connection.getPluginId())
                .externalId(connection.getExternalId())
                .externalName(connection.getExternalName())
                .scopeType(toCoreScopeType(connection.getScopeType()))
                .companyId(connection.getCompanyId())
                .userId(connection.getUserId())
                .active(connection.getStatus() == ConnectionStatus.ACTIVE)
                .build();
    }

    private CredentialInfo toCredentialInfo(PluginConnection connection, OAuthCredential credential) {
        Map<String, String> metadata = parseMetadata(connection.getMetadata());

        return CredentialInfo.builder()
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

    private ScopeType toCoreScopeType(com.daou.dop.global.apps.domain.enums.ScopeType domainType) {
        if (domainType == null) return ScopeType.WORKSPACE;
        return switch (domainType) {
            case WORKSPACE -> ScopeType.WORKSPACE;
            case USER -> ScopeType.USER;
        };
    }

    private com.daou.dop.global.apps.domain.enums.ScopeType toDomainScopeType(ScopeType coreType) {
        if (coreType == null) return com.daou.dop.global.apps.domain.enums.ScopeType.WORKSPACE;
        return switch (coreType) {
            case WORKSPACE -> com.daou.dop.global.apps.domain.enums.ScopeType.WORKSPACE;
            case USER -> com.daou.dop.global.apps.domain.enums.ScopeType.USER;
        };
    }
}
