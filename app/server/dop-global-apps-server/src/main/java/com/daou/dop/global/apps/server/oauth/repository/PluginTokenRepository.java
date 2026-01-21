package com.daou.dop.global.apps.server.oauth.repository;

import com.daou.dop.global.apps.server.oauth.entity.PluginToken;
import com.daou.dop.global.apps.server.oauth.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PluginTokenRepository extends JpaRepository<PluginToken, Long> {

    Optional<PluginToken> findByPluginIdAndExternalId(String pluginId, String externalId);

    List<PluginToken> findAllByPluginIdAndStatus(String pluginId, TokenStatus status);

    List<PluginToken> findAllByPluginId(String pluginId);
}
