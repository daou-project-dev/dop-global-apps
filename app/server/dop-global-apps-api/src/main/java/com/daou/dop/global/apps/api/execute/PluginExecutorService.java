package com.daou.dop.global.apps.api.execute;

import com.daou.dop.global.apps.api.connection.service.ConnectionService;
import com.daou.dop.global.apps.plugin.sdk.CredentialContext;
import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;
import com.daou.dop.global.apps.plugin.sdk.PluginExecutor;
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
    private final ConnectionService connectionService;
    private final Map<String, PluginExecutor> executorMap = new ConcurrentHashMap<>();

    public PluginExecutorService(PluginManager pluginManager, ConnectionService connectionService) {
        this.pluginManager = pluginManager;
        this.connectionService = connectionService;
    }

    @PostConstruct
    public void initialize() {
        List<PluginExecutor> executors = pluginManager.getExtensions(PluginExecutor.class);

        for (PluginExecutor executor : executors) {
            String pluginId = executor.getPluginId();
            executorMap.put(pluginId, executor);
            log.info("Registered PluginExecutor: {} (actions: {})", pluginId, executor.getSupportedActions());
        }

        log.info("Total {} PluginExecutors registered", executorMap.size());
    }

    public ExecuteResponse execute(ExecuteRequest request) {
        String pluginId = request.pluginId();

        if (pluginId == null || pluginId.isBlank()) {
            return ExecuteResponse.error(400, "pluginId is required");
        }

        PluginExecutor executor = executorMap.get(pluginId);

        if (executor == null) {
            return ExecuteResponse.error(404, "Plugin not found: " + pluginId);
        }

        String action = request.action();
        if (action == null || action.isBlank()) {
            return ExecuteResponse.error(400, "action is required");
        }

        if (!executor.supportsAction(action)) {
            return ExecuteResponse.error(400, "Unsupported action: " + action);
        }

        // Credential 조회 및 요청에 주입
        ExecuteRequest enrichedRequest = enrichWithCredential(request);

        try {
            log.debug("Executing plugin: {}, action: {}", pluginId, action);
            return executor.execute(enrichedRequest);
        } catch (Exception e) {
            log.error("Plugin execution failed: {}", pluginId, e);
            return ExecuteResponse.error("Execution failed: " + e.getMessage());
        }
    }

    private ExecuteRequest enrichWithCredential(ExecuteRequest request) {
        if (request.credential() != null) {
            return request;
        }

        String externalId = request.getStringParam("externalId");
        if (externalId == null || externalId.isBlank()) {
            return request;
        }

        String pluginId = request.pluginId();
        Optional<CredentialContext> credential = connectionService.getCredentialContext(pluginId, externalId);

        return credential
                .map(ctx -> ExecuteRequest.builder()
                        .pluginId(request.pluginId())
                        .action(request.action())
                        .params(request.params())
                        .credential(ctx)
                        .build())
                .orElse(request);
    }

    public boolean hasPlugin(String pluginId) {
        return executorMap.containsKey(pluginId);
    }

    public List<String> getSupportedActions(String pluginId) {
        PluginExecutor executor = executorMap.get(pluginId);
        return executor != null ? executor.getSupportedActions() : List.of();
    }
}
