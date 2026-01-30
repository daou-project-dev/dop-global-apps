package com.daou.dop.gapps.core.oauth;

import java.time.Duration;

/**
 * PKCE (Proof Key for Code Exchange) 저장소 인터페이스
 * code_verifier를 state 기반으로 저장/조회
 */
public interface PkceStorage {

    /**
     * code_verifier 저장
     *
     * @param state state 값 (키로 사용)
     * @param codeVerifier PKCE code_verifier
     * @param ttl 유효 시간
     */
    void store(String state, String codeVerifier, Duration ttl);

    /**
     * code_verifier 조회 및 삭제 (일회성)
     *
     * @param state state 값
     * @return code_verifier 또는 null
     */
    String consumeCodeVerifier(String state);
}
