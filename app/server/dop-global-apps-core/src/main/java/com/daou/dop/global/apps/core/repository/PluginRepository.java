package com.daou.dop.global.apps.core.repository;

import com.daou.dop.global.apps.domain.enums.PluginStatus;
import com.daou.dop.global.apps.domain.plugin.Plugin;

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
