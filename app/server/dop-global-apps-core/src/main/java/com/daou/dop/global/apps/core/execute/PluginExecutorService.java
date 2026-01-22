package com.daou.dop.global.apps.core.execute;

import com.daou.dop.global.apps.core.credential.CredentialProvider;
import com.daou.dop.global.apps.core.dto.CredentialInfo;
import com.daou.dop.global.apps.core.dto.ExecuteCommand;
import com.daou.dop.global.apps.core.dto.ExecuteResult;
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
    private final CredentialProvider credentialProvider;
    private final Map<String, PluginExecutor> executorMap = new ConcurrentHashMap<>();

    public PluginExecutorService(PluginManager pluginManager, CredentialProvider credentialProvider) {
        this.pluginManager = pluginManager;
        this.credentialProvider = credentialProvider;
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

    /**
     * 플러그인 API 실행 (core DTO 사용)
     */
    public ExecuteResult execute(ExecuteCommand command) {
        String pluginId = command.pluginId();

        if (pluginId == null || pluginId.isBlank()) {
            return ExecuteResult.error(400, "pluginId is required");
        }

        PluginExecutor executor = executorMap.get(pluginId);

        if (executor == null) {
            return ExecuteResult.error(404, "Plugin not found: " + pluginId);
        }

        String action = command.action();
        if (action == null || action.isBlank()) {
            return ExecuteResult.error(400, "action is required");
        }

        if (!executor.supportsAction(action)) {
            return ExecuteResult.error(400, "Unsupported action: " + action);
        }

        // core DTO → plugin-sdk DTO 변환
        ExecuteRequest request = toExecuteRequest(command);

        // Credential 조회 및 요청에 주입
        ExecuteRequest enrichedRequest = enrichWithCredential(request);

        try {
            log.debug("Executing plugin: {}, action: {}", pluginId, action);
            ExecuteResponse response = executor.execute(enrichedRequest);
            return toExecuteResult(response);
        } catch (Exception e) {
            log.error("Plugin execution failed: {}", pluginId, e);
            return ExecuteResult.error("Execution failed: " + e.getMessage());
        }
    }

    private ExecuteRequest enrichWithCredential(ExecuteRequest request) {
        if (request.credential() != null) {
            return request;
        }

        String externalId = request.getStringParam("externalId");
        if (externalId == null || externalId.isBlank()) {
            log.warn("Skipping credential enrichment: missing or blank externalId for pluginId='{}', action='{}'", request.pluginId(), request.action());
            return request;
        }

        String pluginId = request.pluginId();
        Optional<CredentialInfo> credential = credentialProvider.getCredentialInfo(pluginId, externalId);

        return credential
                .map(info -> ExecuteRequest.builder()
                        .pluginId(request.pluginId())
                        .action(request.action())
                        .params(request.params())
                        .credential(toCredentialContext(info))
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

    // ========== 변환 메서드 ==========

    private ExecuteRequest toExecuteRequest(ExecuteCommand command) {
        return ExecuteRequest.builder()
                .pluginId(command.pluginId())
                .action(command.action())
                .params(command.params())
                .build();
    }

    private ExecuteResult toExecuteResult(ExecuteResponse response) {
        return new ExecuteResult(
                response.success(),
                response.statusCode(),
                response.body(),
                response.error()
        );
    }

    private CredentialContext toCredentialContext(CredentialInfo info) {
        return CredentialContext.builder()
                .accessToken(info.accessToken())
                .refreshToken(info.refreshToken())
                .apiKey(info.apiKey())
                .expiresAt(info.expiresAt())
                .externalId(info.externalId())
                .metadata(info.metadata())
                .build();
    }
}
