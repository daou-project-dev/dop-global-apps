package com.daou.dop.global.apps.core.webhook;

import com.daou.dop.global.apps.plugin.sdk.WebhookImmediateResponse;

/**
 * 웹훅 처리 결과
 */
public record WebhookResult(
        int statusCode,
        String contentType,
        String body,
        boolean processed
) {
    public static WebhookResult ok() {
        return new WebhookResult(200, "application/json", "{\"status\":\"ok\"}", true);
    }

    public static WebhookResult forbidden(String message) {
        return new WebhookResult(403, "application/json",
                "{\"error\":\"" + message + "\"}", false);
    }

    public static WebhookResult badRequest(String message) {
        return new WebhookResult(400, "application/json",
                "{\"error\":\"" + message + "\"}", false);
    }

    public static WebhookResult notFound(String message) {
        return new WebhookResult(404, "application/json",
                "{\"error\":\"" + message + "\"}", false);
    }

    public static WebhookResult error(String message) {
        return new WebhookResult(500, "application/json",
                "{\"error\":\"" + message + "\"}", false);
    }

    public static WebhookResult of(WebhookImmediateResponse response) {
        return new WebhookResult(
                response.statusCode(),
                response.contentType() != null ? response.contentType() : "application/json",
                response.body(),
                true
        );
    }
}
