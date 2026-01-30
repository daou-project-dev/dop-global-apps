package com.daou.dop.gapps.plugin.sdk;

/**
 * OAuth 처리 중 발생하는 예외
 */
public class OAuthException extends Exception {

    private final String errorCode;

    public OAuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public OAuthException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
