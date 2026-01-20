package com.daou.dop.global.apps.core.execute.dto;

/**
 * 플러그인 API 실행 응답
 */
public record ExecuteResponse(
        boolean success,
        int statusCode,
        String body,
        String error
) {
    public static ExecuteResponse success(int statusCode, String body) {
        return new ExecuteResponse(true, statusCode, body, null);
    }

    public static ExecuteResponse error(int statusCode, String error) {
        return new ExecuteResponse(false, statusCode, null, error);
    }

    public static ExecuteResponse error(String error) {
        return new ExecuteResponse(false, 500, null, error);
    }
}
