package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.domain.connection.PluginConnection;
import com.daou.dop.global.apps.domain.connection.PluginConnectionRepository;
import com.daou.dop.global.apps.domain.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PluginConnection JPA Repository
 */
@Repository
public interface JpaPluginConnectionRepository extends JpaRepository<PluginConnection, Long>, PluginConnectionRepository {

    @Override
    Optional<PluginConnection> findByPluginIdAndExternalId(String pluginId, String externalId);

    @Override
    Optional<PluginConnection> findByPluginIdAndCompanyIdAndExternalId(
            String pluginId, Long companyId, String externalId);

    @Override
    Optional<PluginConnection> findByPluginIdAndCompanyIdAndUserIdAndExternalId(
            String pluginId, Long companyId, Long userId, String externalId);

    @Override
    List<PluginConnection> findByPluginId(String pluginId);

    @Override
    List<PluginConnection> findByPluginIdAndStatus(String pluginId, ConnectionStatus status);

    @Override
    List<PluginConnection> findByCompanyId(Long companyId);

    @Override
    List<PluginConnection> findByCompanyIdAndStatus(Long companyId, ConnectionStatus status);

    @Override
    List<PluginConnection> findByUserId(Long userId);

    @Override
    boolean existsByPluginIdAndExternalId(String pluginId, String externalId);
}
