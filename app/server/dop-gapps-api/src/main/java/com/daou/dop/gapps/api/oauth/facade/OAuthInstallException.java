package com.daou.dop.gapps.api.oauth.facade;

import org.springframework.http.HttpStatus;

/**
 * OAuth 설치 예외
 * HTTP 상태 코드와 메시지를 포함
 */
public class OAuthInstallException extends RuntimeException {

    private final HttpStatus status;

    public OAuthInstallException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public OAuthInstallException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
