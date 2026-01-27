package com.daou.dop.global.apps.core.dto;

/**
 * 인증 설정 정보 (api 표시용)
 *
 * @param url    인증 URL
 * @param method 인증 방식 ("redirect" - 외부 redirect, "submit" - 자격 증명 제출)
 */
public record AuthConfigInfo(
        String url,
        String method
) {
    public static final String METHOD_REDIRECT = "redirect";
    public static final String METHOD_SUBMIT = "submit";

    public static AuthConfigInfo redirect(String url) {
        return new AuthConfigInfo(url, METHOD_REDIRECT);
    }

    public static AuthConfigInfo submit(String url) {
        return new AuthConfigInfo(url, METHOD_SUBMIT);
    }
}
