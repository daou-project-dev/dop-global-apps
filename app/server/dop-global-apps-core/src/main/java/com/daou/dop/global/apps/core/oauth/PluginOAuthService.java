package com.daou.dop.global.apps.core.oauth;

import com.daou.dop.global.apps.core.dto.OAuthTokenInfo;
import com.daou.dop.global.apps.core.dto.PluginConfigInfo;

/**
 * 플러그인 OAuth 서비스 인터페이스
 * OAuthHandler를 래핑하여 api가 plugin-sdk에 직접 의존하지 않도록 함
 */
public interface PluginOAuthService {

    /**
     * 플러그인 OAuth 지원 여부
     */
    boolean supportsOAuth(String pluginId);

    /**
     * OAuth 인증 URL 생성
     *
     * @param pluginId    플러그인 ID
     * @param config      플러그인 설정
     * @param state       CSRF 방지용 state
     * @param redirectUri 콜백 URL
     * @return 인증 URL
     */
    String buildAuthorizationUrl(String pluginId, PluginConfigInfo config, String state, String redirectUri);

    /**
     * 인증 코드로 토큰 교환
     *
     * @param pluginId    플러그인 ID
     * @param config      플러그인 설정
     * @param code        인증 코드
     * @param redirectUri 콜백 URL
     * @return 토큰 정보
     * @throws OAuthException 토큰 교환 실패 시
     */
    OAuthTokenInfo exchangeCode(String pluginId, PluginConfigInfo config, String code, String redirectUri)
            throws OAuthException;
}
