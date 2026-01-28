package com.daou.dop.global.apps.infrastructure.oauth;

import com.daou.dop.global.apps.core.oauth.PkceStorage;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 PKCE 저장소 구현
 * <p>
 * <strong>주의:</strong> 분산/프로덕션 환경에서는 세션 유실 및 일관성 문제로 인해 사용하면 안 됩니다.
 * Redis 등 외부 저장소 기반 구현으로 반드시 대체해야 하며, 이 클래스는 로컬 개발/테스트 용도로만 사용하십시오.
 *
 * @deprecated 로컬 개발 또는 단일 인스턴스 테스트 용도로만 사용하십시오.
 * 프로덕션 또는 분산 환경에서는 Redis 등 영속/공유 저장소 구현으로 교체해야 합니다.
 */
@Component
@Deprecated
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
