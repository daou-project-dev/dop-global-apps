package com.daou.dop.global.apps.domain.plugin;

import com.daou.dop.global.apps.domain.enums.PluginStatus;

import java.util.List;
import java.util.Optional;

/**
 * Plugin Repository 인터페이스
 *
 * <p>infrastructure 모듈에서 JPA 구현
 */
public interface PluginRepository {

    Optional<Plugin> findById(Long id);

    Optional<Plugin> findByPluginId(String pluginId);

    List<Plugin> findByStatus(PluginStatus status);

    List<Plugin> findAll();

    boolean existsByPluginId(String pluginId);

    Plugin save(Plugin plugin);

    void deleteById(Long id);
}
