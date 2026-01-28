package com.daou.dop.global.apps.core.oauth;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

/**
 * PKCE 서비스 구현
 */
@Service
public class PkceServiceImpl implements PkceService {

    private final PkceStorage pkceStorage;

    public PkceServiceImpl(PkceStorage pkceStorage) {
        this.pkceStorage = pkceStorage;
    }

    @Override
    public String generateAndStoreCodeChallenge(String state, Duration ttl) {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        pkceStorage.store(state, codeVerifier, ttl);
        return codeChallenge;
    }

    @Override
    public String consumeCodeVerifier(String state) {
        return pkceStorage.consumeCodeVerifier(state);
    }

    /**
     * PKCE code_verifier 생성 (43-128자 URL-safe random string)
     */
    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[64];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    /**
     * PKCE code_challenge 생성 (code_verifier의 SHA256 해시)
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
