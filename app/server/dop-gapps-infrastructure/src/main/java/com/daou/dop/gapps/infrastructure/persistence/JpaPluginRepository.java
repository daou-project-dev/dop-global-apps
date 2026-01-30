package com.daou.dop.gapps.infrastructure.persistence;

import com.daou.dop.gapps.core.repository.PluginRepository;
import com.daou.dop.gapps.domain.enums.PluginStatus;
import com.daou.dop.gapps.domain.plugin.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Plugin JPA Repository
 */
@Repository
public interface JpaPluginRepository extends JpaRepository<Plugin, Long>, PluginRepository {

    Optional<Plugin> findByPluginId(String pluginId);

    List<Plugin> findByStatus(PluginStatus status);

    boolean existsByPluginId(String pluginId);
}
