package com.daou.dop.global.apps.core.plugin;

import com.daou.dop.global.apps.core.dto.PluginConfigInfo;
import com.daou.dop.global.apps.core.repository.PluginRepository;
import com.daou.dop.global.apps.domain.enums.PluginStatus;
import com.daou.dop.global.apps.domain.plugin.Plugin;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 플러그인 관리 서비스
 * Plugin Entity를 PluginConfigInfo DTO로 변환
 */
@Service
@Transactional(readOnly = true)
public class PluginService {

    private static final Logger log = LoggerFactory.getLogger(PluginService.class);

    private final PluginRepository pluginRepository;
    private final ObjectMapper objectMapper;

    public PluginService(PluginRepository pluginRepository, ObjectMapper objectMapper) {
        this.pluginRepository = pluginRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * pluginId로 PluginConfigInfo 조회
     */
    public Optional<PluginConfigInfo> getPluginConfig(String pluginId) {
        return pluginRepository.findByPluginId(pluginId)
                .map(this::toPluginConfigInfo);
    }

    /**
     * 활성 플러그인 설정 목록 조회
     */
    public List<PluginConfigInfo> findAllActiveConfigs() {
        return pluginRepository.findByStatus(PluginStatus.ACTIVE)
                .stream()
                .map(this::toPluginConfigInfo)
                .toList();
    }

    /**
     * 플러그인 존재 여부 확인
     */
    public boolean existsByPluginId(String pluginId) {
        return pluginRepository.existsByPluginId(pluginId);
    }

    // ========== 내부 메서드 ==========

    private PluginConfigInfo toPluginConfigInfo(Plugin plugin) {
        Map<String, String> secrets = parseSecrets(plugin.getSecrets());
        Map<String, Object> metadata = parseMetadata(plugin.getMetadata());

        return PluginConfigInfo.builder()
                .pluginId(plugin.getPluginId())
                .displayName(plugin.getDisplayName())
                .clientId(plugin.getClientId())
                .clientSecret(plugin.getClientSecret())
                .secrets(secrets)
                .metadata(metadata)
                .build();
    }

    private Map<String, String> parseSecrets(String secretsJson) {
        if (secretsJson == null || secretsJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(secretsJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse secrets JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse metadata JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
