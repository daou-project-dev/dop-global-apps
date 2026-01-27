package com.daou.dop.global.apps.api.plugin.dto;

import com.daou.dop.global.apps.core.dto.AuthConfigInfo;

/**
 * 인증 설정 응답 DTO
 *
 * @param url    인증 URL
 * @param method 인증 방식 ("redirect" - 외부 redirect, "submit" - 자격 증명 제출)
 */
public record AuthConfig(
        String url,
        String method
) {
    /**
     * Core DTO → API DTO 변환
     */
    public static AuthConfig from(AuthConfigInfo info) {
        if (info == null) {
            return null;
        }
        return new AuthConfig(info.url(), info.method());
    }
}
