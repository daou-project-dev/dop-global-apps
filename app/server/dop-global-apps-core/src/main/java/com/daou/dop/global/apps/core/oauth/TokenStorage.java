package com.daou.dop.global.apps.core.oauth;

import java.util.List;
import java.util.Optional;

/**
 * OAuth 토큰 저장소 인터페이스
 */
public interface TokenStorage {

    /**
     * 토큰 저장 (신규 저장 또는 업데이트)
     *
     * @param tokenInfo 토큰 정보
     */
    void save(TokenInfo tokenInfo);

    /**
     * 외부 ID로 토큰 조회
     *
     * @param pluginId 플러그인 ID
     * @param externalId 외부 시스템 ID (예: Slack teamId)
     * @return 토큰 정보
     */
    Optional<TokenInfo> findByExternalId(String pluginId, String externalId);

    /**
     * 플러그인의 모든 토큰 조회
     *
     * @param pluginId 플러그인 ID
     * @return 토큰 목록
     */
    List<TokenInfo> findAllByPluginId(String pluginId);

    /**
     * 토큰 폐기
     *
     * @param pluginId 플러그인 ID
     * @param externalId 외부 시스템 ID
     */
    void revoke(String pluginId, String externalId);
}
