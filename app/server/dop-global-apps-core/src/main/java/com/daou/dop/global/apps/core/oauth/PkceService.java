package com.daou.dop.global.apps.core.oauth;

import java.time.Duration;

/**
 * PKCE (Proof Key for Code Exchange) 서비스 인터페이스
 * code_verifier 생성/저장/조회
 */
public interface PkceService {

    /**
     * PKCE code_verifier 생성 및 저장, code_challenge 반환
     *
     * @param state state 값 (키로 사용)
     * @param ttl 유효 시간
     * @return code_challenge (SHA256 해시, Base64 URL-safe 인코딩)
     */
    String generateAndStoreCodeChallenge(String state, Duration ttl);

    /**
     * code_verifier 조회 및 소비 (일회용)
     *
     * @param state state 값
     * @return code_verifier 또는 null
     */
    String consumeCodeVerifier(String state);
}
