package com.daou.dop.global.apps.api.plugin.service;

import com.daou.dop.global.apps.api.plugin.PluginRegistry;
import com.daou.dop.global.apps.plugin.sdk.PluginExecutor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * 플러그인 JAR 내 리소스 파일 로드 서비스
 * - form-config.json: 인증/설정 폼
 * - test-form.json: API 테스트 폼
 */
@Service
public class PluginResourceService {

    private static final Logger log = LoggerFactory.getLogger(PluginResourceService.class);

    private static final String FORM_CONFIG_FILE = "form-config.json";
    private static final String TEST_FORM_FILE = "test-form.json";

    private final PluginRegistry pluginRegistry;
    private final ObjectMapper objectMapper;

    public PluginResourceService(PluginRegistry pluginRegistry, ObjectMapper objectMapper) {
        this.pluginRegistry = pluginRegistry;
        this.objectMapper = objectMapper;
    }

    /**
     * 플러그인 폼 설정 조회
     */
    public Optional<Map<String, Object>> getFormConfig(String pluginId) {
        return loadResource(pluginId, FORM_CONFIG_FILE);
    }

    /**
     * 플러그인 테스트 폼 조회
     */
    public Optional<Map<String, Object>> getTestForm(String pluginId) {
        return loadResource(pluginId, TEST_FORM_FILE);
    }

    /**
     * 플러그인 JAR 내 리소스 파일 로드
     */
    private Optional<Map<String, Object>> loadResource(String pluginId, String resourceName) {
        Optional<PluginExecutor> executorOpt = pluginRegistry.findPluginExecutor(pluginId);

        if (executorOpt.isEmpty()) {
            log.warn("Plugin not found: {}", pluginId);
            return Optional.empty();
        }

        PluginExecutor executor = executorOpt.get();
        ClassLoader classLoader = executor.getClass().getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream(resourceName)) {
            if (is == null) {
                log.warn("Resource not found: {} for plugin: {}", resourceName, pluginId);
                return Optional.empty();
            }

            Map<String, Object> result = objectMapper.readValue(is, new TypeReference<>() {});
            log.debug("Loaded resource: {} for plugin: {}", resourceName, pluginId);
            return Optional.of(result);

        } catch (Exception e) {
            log.error("Failed to load resource: {} for plugin: {}", resourceName, pluginId, e);
            return Optional.empty();
        }
    }
}
