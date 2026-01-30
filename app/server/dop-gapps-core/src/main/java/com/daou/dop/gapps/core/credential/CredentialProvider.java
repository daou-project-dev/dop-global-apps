package com.daou.dop.gapps.core.credential;

import com.daou.dop.gapps.core.dto.CredentialInfo;

import java.util.Optional;

/**
 * Credential 조회 포트
 * PluginExecutorService가 의존, ConnectionService가 구현
 */
public interface CredentialProvider {
    Optional<CredentialInfo> getCredentialInfo(String pluginId, String externalId);
}
