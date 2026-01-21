package com.daou.dop.global.apps.core.oauth;

import org.pf4j.ExtensionPoint;

/**
 * OAuth 처리를 위한 플러그인 확장점
 * 각 플러그인(Slack, Teams, Discord 등)에서 구현
 */
public interface OAuthHandler extends ExtensionPoint {

    /**
     * 플러그인 ID 반환
     *
     * @return 플러그인 고유 식별자 (예: "slack", "teams")
     */
    String getPluginId();

    /**
     * OAuth 인증 URL 생성
     *
     * @param state CSRF 방지용 state 값
     * @param redirectUri 콜백 URL
     * @return 인증 URL
     */
    String buildAuthorizationUrl(String state, String redirectUri);

    /**
     * 인증 코드를 토큰으로 교환
     *
     * @param code 인증 코드
     * @param redirectUri 콜백 URL
     * @return 토큰 정보
     * @throws OAuthException 토큰 교환 실패 시
     */
    TokenInfo exchangeCode(String code, String redirectUri) throws OAuthException;
}
