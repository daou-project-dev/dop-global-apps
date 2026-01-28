package com.daou.dop.global.apps.plugin.sdk;

/**
 * 웹훅 즉시 응답
 * URL 검증, challenge 응답 등에 사용
 *
 * @param statusCode  HTTP 상태 코드
 * @param contentType Content-Type 헤더
 * @param body        응답 본문
 */
public record WebhookImmediateResponse(
        int statusCode,
        String contentType,
        String body
) {
    /**
     * JSON 응답 (200 OK)
     */
    public static WebhookImmediateResponse json(String body) {
        return new WebhookImmediateResponse(200, "application/json", body);
    }

    /**
     * Plain text 응답 (200 OK)
     */
    public static WebhookImmediateResponse text(String body) {
        return new WebhookImmediateResponse(200, "text/plain", body);
    }

    /**
     * 빈 응답 (200 OK)
     */
    public static WebhookImmediateResponse ok() {
        return new WebhookImmediateResponse(200, null, null);
    }

    /**
     * 커스텀 상태 코드로 응답
     */
    public static WebhookImmediateResponse status(int statusCode) {
        return new WebhookImmediateResponse(statusCode, null, null);
    }
}
