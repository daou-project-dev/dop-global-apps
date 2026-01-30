package com.daou.dop.gapps.infrastructure.oauth;

import com.daou.dop.gapps.core.oauth.StateStorage;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 State 저장소 구현
 * 분산 환경에서는 Redis 등으로 대체 필요
 */
@Component
public class InMemoryStateStorage implements StateStorage {

    private final Map<String, StateEntry> stateStore = new ConcurrentHashMap<>();

    @Override
    public String generateAndStore(String pluginId, Duration ttl) {
        String state = UUID.randomUUID().toString();
        String key = buildKey(pluginId, state);
        Instant expiresAt = Instant.now().plus(ttl);
        stateStore.put(key, new StateEntry(state, expiresAt));

        // 만료된 항목 정리 (간단한 구현)
        cleanupExpired();

        return state;
    }

    @Override
    public boolean validateAndConsume(String pluginId, String state) {
        if (state == null || state.isBlank()) {
            return false;
        }

        String key = buildKey(pluginId, state);
        StateEntry entry = stateStore.remove(key);

        if (entry == null) {
            return false;
        }

        return Instant.now().isBefore(entry.expiresAt());
    }

    private String buildKey(String pluginId, String state) {
        return pluginId + ":" + state;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        stateStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
    }

    private record StateEntry(String state, Instant expiresAt) {
    }
}
