package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.core.repository.PluginConnectionRepository;
import com.daou.dop.global.apps.domain.connection.PluginConnection;
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
}
