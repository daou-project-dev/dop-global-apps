package com.daou.dop.global.apps.core.dto;

/**
 * 플러그인 API 실행 결과 (core → api)
 *
 * @param success    성공 여부
 * @param statusCode HTTP 상태 코드
 * @param body       응답 본문
 * @param error      에러 메시지
 */
public record ExecuteResult(
        boolean success,
        int statusCode,
        String body,
        String error
) {
    public static ExecuteResult success(int statusCode, String body) {
        return new ExecuteResult(true, statusCode, body, null);
    }

    public static ExecuteResult success(String body) {
        return success(200, body);
    }

    public static ExecuteResult error(int statusCode, String error) {
        return new ExecuteResult(false, statusCode, null, error);
    }

    public static ExecuteResult error(String error) {
        return error(500, error);
    }
}
