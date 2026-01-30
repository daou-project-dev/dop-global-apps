package com.daou.dop.gapps.core.repository;

import com.daou.dop.gapps.domain.connection.PluginConnection;
import com.daou.dop.gapps.domain.enums.ConnectionStatus;

import java.util.List;
import java.util.Optional;

/**
 * PluginConnection Repository Port
 */
public interface PluginConnectionRepository {

    Optional<PluginConnection> findById(Long id);

    Optional<PluginConnection> findByPluginIdAndExternalId(String pluginId, String externalId);

    List<PluginConnection> findByPluginIdAndStatus(String pluginId, ConnectionStatus status);

    List<PluginConnection> findByCompanyIdAndStatus(Long companyId, ConnectionStatus status);

    List<PluginConnection> findByCompanyIdIsNullAndStatus(ConnectionStatus status);

    List<PluginConnection> findByStatus(ConnectionStatus status);

    PluginConnection save(PluginConnection connection);

    void deleteById(Long id);
}
