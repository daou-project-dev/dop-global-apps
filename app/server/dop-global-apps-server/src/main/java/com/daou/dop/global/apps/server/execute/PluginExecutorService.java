package com.daou.dop.global.apps.server.execute;

import com.daou.dop.global.apps.core.execute.PluginExecutor;
import com.daou.dop.global.apps.core.execute.dto.ExecuteRequest;
import com.daou.dop.global.apps.core.execute.dto.ExecuteResponse;
import com.daou.dop.global.apps.core.oauth.TokenInfo;
import com.daou.dop.global.apps.core.oauth.TokenStorage;
import jakarta.annotation.PostConstruct;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PluginExecutorService {

    private static final Logger log = LoggerFactory.getLogger(PluginExecutorService.class);

    private final PluginManager pluginManager;
    private final TokenStorage tokenStorage;
    private final Map<String, PluginExecutor> executorMap = new ConcurrentHashMap<>();

    public PluginExecutorService(PluginManager pluginManager, TokenStorage tokenStorage) {
        this.pluginManager = pluginManager;
        this.tokenStorage = tokenStorage;
    }

    @PostConstruct
    public void initialize() {
        List<PluginExecutor> executors = pluginManager.getExtensions(PluginExecutor.class);

        for (PluginExecutor executor : executors) {
            String pluginName = executor.getPluginName();
            executorMap.put(pluginName, executor);
            log.info("Registered PluginExecutor: {}", pluginName);
        }

        log.info("Total {} PluginExecutors registered", executorMap.size());
    }

    public ExecuteResponse execute(ExecuteRequest request) {
        String pluginName = request.plugin();

        if (pluginName == null || pluginName.isBlank()) {
            return ExecuteResponse.error(400, "plugin is required");
        }

        PluginExecutor executor = executorMap.get(pluginName);

        if (executor == null) {
            return ExecuteResponse.error(404, "Plugin not found: " + pluginName);
        }

        // 토큰 조회 및 요청에 주입
        ExecuteRequest enrichedRequest = enrichWithToken(request);
        if (enrichedRequest.accessToken() == null && request.teamId() != null) {
            return ExecuteResponse.error(401, "Token not found for teamId: " + request.teamId());
        }

        try {
            log.debug("Executing plugin: {}, uri: {}", pluginName, request.uri());
            return executor.execute(enrichedRequest);
        } catch (Exception e) {
            log.error("Plugin execution failed: {}", pluginName, e);
            return ExecuteResponse.error("Execution failed: " + e.getMessage());
        }
    }

    private ExecuteRequest enrichWithToken(ExecuteRequest request) {
        if (request.teamId() == null || request.teamId().isBlank()) {
            return request;
        }

        // pluginName을 pluginId로 사용하여 토큰 조회
        String pluginId = request.plugin();
        Optional<TokenInfo> tokenInfo = tokenStorage.findByExternalId(pluginId, request.teamId());

        return tokenInfo
                .map(info -> ExecuteRequest.builder()
                        .plugin(request.plugin())
                        .method(request.method())
                        .uri(request.uri())
                        .body(request.body())
                        .teamId(request.teamId())
                        .accessToken(info.accessToken())
                        .build())
                .orElse(request);
    }

    public boolean hasPlugin(String pluginName) {
        return executorMap.containsKey(pluginName);
    }
}
