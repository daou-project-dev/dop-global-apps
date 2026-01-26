package com.daou.dop.global.apps.infrastructure.oauth;

import com.daou.dop.global.apps.core.oauth.PkceStorage;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 PKCE 저장소 구현
 * 분산 환경에서는 Redis 등으로 대체 필요
 */
@Component
public class InMemoryPkceStorage implements PkceStorage {

    private final Map<String, PkceEntry> storage = new ConcurrentHashMap<>();

    @Override
    public void store(String state, String codeVerifier, Duration ttl) {
        Instant expiresAt = Instant.now().plus(ttl);
        storage.put(state, new PkceEntry(codeVerifier, expiresAt));
        cleanupExpired();
    }

    @Override
    public String consumeCodeVerifier(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }

        PkceEntry entry = storage.remove(state);
        if (entry == null) {
            return null;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            return null;
        }

        return entry.codeVerifier();
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        storage.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
    }

    private record PkceEntry(String codeVerifier, Instant expiresAt) {
    }
}
