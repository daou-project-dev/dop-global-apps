package com.daou.dop.gapps.plugin.sdk;

/**
 * 플러그인 API 실행 응답
 *
 * @param success    성공 여부
 * @param statusCode HTTP 상태 코드
 * @param body       응답 본문 (JSON)
 * @param error      에러 메시지 (실패 시)
 */
public record ExecuteResponse(
        boolean success,
        int statusCode,
        String body,
        String error
) {
    /**
     * 성공 응답 생성
     */
    public static ExecuteResponse success(int statusCode, String body) {
        return new ExecuteResponse(true, statusCode, body, null);
    }

    /**
     * 성공 응답 생성 (200 OK)
     */
    public static ExecuteResponse success(String body) {
        return success(200, body);
    }

    /**
     * 에러 응답 생성
     */
    public static ExecuteResponse error(int statusCode, String error) {
        return new ExecuteResponse(false, statusCode, null, error);
    }

    /**
     * 에러 응답 생성 (500 Internal Server Error)
     */
    public static ExecuteResponse error(String error) {
        return error(500, error);
    }
}
