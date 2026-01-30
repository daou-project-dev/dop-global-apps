package com.daou.dop.gapps.core.repository;

import com.daou.dop.gapps.domain.enums.PluginStatus;
import com.daou.dop.gapps.domain.plugin.Plugin;

import java.util.List;
import java.util.Optional;

/**
 * Plugin Repository Port
 */
public interface PluginRepository {

    Optional<Plugin> findByPluginId(String pluginId);

    List<Plugin> findByStatus(PluginStatus status);

    Plugin save(Plugin plugin);

    boolean existsByPluginId(String pluginId);
}
