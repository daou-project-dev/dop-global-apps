package com.daou.dop.global.apps.api.oauth.facade;

/**
 * OAuth 설치 Facade
 * core 모듈 서비스들을 조합하여 OAuth 플로우 처리
 */
public interface OAuthInstallFacade {

    /**
     * OAuth 설치 시작
     *
     * @param pluginId    플러그인 ID
     * @param redirectUri 콜백 URL
     * @return 인증 URL (리다이렉트 대상)
     * @throws OAuthInstallException 플러그인 미지원 또는 설정 없음
     */
    String startInstall(String pluginId, String redirectUri);

    /**
     * OAuth 콜백 처리
     *
     * @param pluginId    플러그인 ID
     * @param code        인증 코드
     * @param state       state 값
     * @param redirectUri 콜백 URL
     * @return 생성된 connection ID
     * @throws OAuthInstallException 검증 실패 또는 토큰 교환 실패
     */
    Long handleCallback(String pluginId, String code, String state, String redirectUri);
}
