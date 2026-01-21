package com.daou.dop.global.apps.server.oauth;

import com.daou.dop.global.apps.core.oauth.TokenInfo;
import com.daou.dop.global.apps.core.oauth.TokenStorage;
import com.daou.dop.global.apps.server.oauth.entity.PluginToken;
import com.daou.dop.global.apps.server.oauth.entity.TokenStatus;
import com.daou.dop.global.apps.server.oauth.repository.PluginTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA 기반 토큰 저장소 구현
 */
@Component
public class JpaTokenStorage implements TokenStorage {

    private final PluginTokenRepository repository;

    public JpaTokenStorage(PluginTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(TokenInfo tokenInfo) {
        Optional<PluginToken> existing = repository.findByPluginIdAndExternalId(
                tokenInfo.pluginId(), tokenInfo.externalId());

        if (existing.isPresent()) {
            PluginToken token = existing.get();
            token.updateToken(
                    tokenInfo.accessToken(),
                    tokenInfo.refreshToken(),
                    tokenInfo.scope(),
                    tokenInfo.expiresAt()
            );
            repository.save(token);
        } else {
            PluginToken token = PluginToken.builder()
                    .pluginId(tokenInfo.pluginId())
                    .externalId(tokenInfo.externalId())
                    .externalName(tokenInfo.externalName())
                    .accessToken(tokenInfo.accessToken())
                    .refreshToken(tokenInfo.refreshToken())
                    .scope(tokenInfo.scope())
                    .expiresAt(tokenInfo.expiresAt())
                    .installedAt(tokenInfo.installedAt())
                    .metadata(tokenInfo.metadata())
                    .build();
            repository.save(token);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenInfo> findByExternalId(String pluginId, String externalId) {
        return repository.findByPluginIdAndExternalId(pluginId, externalId)
                .filter(token -> token.getStatus() == TokenStatus.ACTIVE)
                .map(this::toTokenInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TokenInfo> findAllByPluginId(String pluginId) {
        return repository.findAllByPluginIdAndStatus(pluginId, TokenStatus.ACTIVE)
                .stream()
                .map(this::toTokenInfo)
                .toList();
    }

    @Override
    @Transactional
    public void revoke(String pluginId, String externalId) {
        repository.findByPluginIdAndExternalId(pluginId, externalId)
                .ifPresent(token -> {
                    token.revoke();
                    repository.save(token);
                });
    }

    private TokenInfo toTokenInfo(PluginToken token) {
        return TokenInfo.builder()
                .pluginId(token.getPluginId())
                .externalId(token.getExternalId())
                .externalName(token.getExternalName())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .scope(token.getScope())
                .expiresAt(token.getExpiresAt())
                .installedAt(token.getInstalledAt())
                .metadata(token.getMetadata())
                .build();
    }
}
