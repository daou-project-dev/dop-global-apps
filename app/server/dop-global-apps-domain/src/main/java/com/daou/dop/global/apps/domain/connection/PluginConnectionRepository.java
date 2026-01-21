package com.daou.dop.global.apps.domain.connection;

import com.daou.dop.global.apps.domain.enums.ConnectionStatus;

import java.util.List;
import java.util.Optional;

/**
 * PluginConnection Repository 인터페이스
 *
 * <p>infrastructure 모듈에서 JPA 구현
 */
public interface PluginConnectionRepository {

    Optional<PluginConnection> findById(Long id);

    Optional<PluginConnection> findByPluginIdAndExternalId(String pluginId, String externalId);

    Optional<PluginConnection> findByPluginIdAndCompanyIdAndExternalId(
            String pluginId, Long companyId, String externalId);

    Optional<PluginConnection> findByPluginIdAndCompanyIdAndUserIdAndExternalId(
            String pluginId, Long companyId, Long userId, String externalId);

    List<PluginConnection> findByPluginId(String pluginId);

    List<PluginConnection> findByPluginIdAndStatus(String pluginId, ConnectionStatus status);

    List<PluginConnection> findByCompanyId(Long companyId);

    List<PluginConnection> findByCompanyIdAndStatus(Long companyId, ConnectionStatus status);

    List<PluginConnection> findByUserId(Long userId);

    boolean existsByPluginIdAndExternalId(String pluginId, String externalId);

    PluginConnection save(PluginConnection connection);

    void deleteById(Long id);
}
