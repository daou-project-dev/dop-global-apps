package com.daou.dop.gapps.domain.enums;

/**
 * 플러그인 인증 방식
 */
public enum AuthType {
    OAUTH2,           // OAuth 2.0 인증
    API_KEY,          // API Key 인증
    SERVICE_ACCOUNT   // Service Account (JSON 키 파일) 인증
}
