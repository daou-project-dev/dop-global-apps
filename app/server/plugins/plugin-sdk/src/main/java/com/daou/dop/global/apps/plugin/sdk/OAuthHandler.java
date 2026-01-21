package com.daou.dop.global.apps.plugin.sdk;

import org.pf4j.ExtensionPoint;

/**
 * OAuth 처리를 위한 플러그인 확장점
 * 각 플러그인(Slack, Teams, Google 등)에서 구현
 */
public interface OAuthHandler extends ExtensionPoint {

    /**
     * 플러그인 ID
     *
     * @return 플러그인 고유 식별자 (예: "slack", "google")
     */
    String getPluginId();

    /**
     * 플러그인 메타데이터
     * 서버가 플러그인 로드 시 DB에 자동 등록하는 데 사용
     *
     * @return 플러그인 메타데이터
     */
    PluginMetadata getMetadata();

    /**
     * OAuth 인증 URL 생성
     *
     * @param config      플러그인 설정 (서버가 DB에서 조회하여 전달)
     * @param state       CSRF 방지용 state
     * @param redirectUri 콜백 URL
     * @return 인증 URL
     */
    String buildAuthorizationUrl(PluginConfig config, String state, String redirectUri);

    /**
     * 인증 코드로 토큰 교환
     *
     * @param config      플러그인 설정
     * @param code        인증 코드
     * @param redirectUri 콜백 URL
     * @return 토큰 정보
     * @throws OAuthException 토큰 교환 실패 시
     */
    TokenInfo exchangeCode(PluginConfig config, String code, String redirectUri) throws OAuthException;

    /**
     * 토큰 갱신 (선택적 구현)
     *
     * @param config       플러그인 설정
     * @param refreshToken 리프레시 토큰
     * @return 새 토큰 정보
     * @throws OAuthException 토큰 갱신 실패 시
     */
    default TokenInfo refreshToken(PluginConfig config, String refreshToken) throws OAuthException {
        throw new UnsupportedOperationException("Token refresh not supported for plugin: " + getPluginId());
    }

    /**
     * 토큰 폐기 (선택적 구현)
     *
     * @param config      플러그인 설정
     * @param accessToken 액세스 토큰
     * @throws OAuthException 토큰 폐기 실패 시
     */
    default void revokeToken(PluginConfig config, String accessToken) throws OAuthException {
        // 기본: 아무것도 안함
    }
}
