package com.daou.dop.gapps.core.oauth;

/**
 * OAuth 처리 중 발생하는 예외
 */
public class OAuthException extends RuntimeException {

    public OAuthException(String message) {
        super(message);
    }

    public OAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
