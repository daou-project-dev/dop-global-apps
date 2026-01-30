package com.daou.dop.gapps.core.oauth;

import java.time.Duration;

/**
 * OAuth State 저장소 인터페이스
 * CSRF 방지를 위한 state 생성 및 검증
 */
public interface StateStorage {

    /**
     * State 생성 및 저장
     *
     * @param pluginId 플러그인 ID
     * @param ttl 유효 시간
     * @return 생성된 state 값
     */
    String generateAndStore(String pluginId, Duration ttl);

    /**
     * State 검증 및 소비 (일회성)
     *
     * @param pluginId 플러그인 ID
     * @param state 검증할 state 값
     * @return 유효한 경우 true
     */
    boolean validateAndConsume(String pluginId, String state);
}
