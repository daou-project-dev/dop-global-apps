package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.domain.enums.PluginStatus;
import com.daou.dop.global.apps.domain.plugin.Plugin;
import com.daou.dop.global.apps.domain.plugin.PluginRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Plugin JPA Repository
 *
 * <p>Spring Data JPA 자동 구현 + domain 인터페이스 구현
 */
@Repository
public interface JpaPluginRepository extends JpaRepository<Plugin, Long>, PluginRepository {

    @Override
    Optional<Plugin> findByPluginId(String pluginId);

    @Override
    List<Plugin> findByStatus(PluginStatus status);

    @Override
    boolean existsByPluginId(String pluginId);
}
