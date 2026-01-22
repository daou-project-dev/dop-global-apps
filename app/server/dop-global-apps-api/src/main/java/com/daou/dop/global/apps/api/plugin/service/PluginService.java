package com.daou.dop.global.apps.api.plugin.service;

import com.daou.dop.global.apps.core.repository.PluginRepository;
import com.daou.dop.global.apps.domain.enums.PluginStatus;
import com.daou.dop.global.apps.domain.plugin.Plugin;
import com.daou.dop.global.apps.plugin.sdk.PluginConfig;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daou.dop.global.apps.api.plugin.dto.PluginResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 플러그인 관리 서비스
 * Plugin Entity를 PluginConfig DTO로 변환하여 플러그인에 전달
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
     * pluginId로 플러그인 조회
     */
    public Optional<Plugin> findByPluginId(String pluginId) {
        return pluginRepository.findByPluginId(pluginId);
    }

    /**
     * 활성 플러그인 목록 조회 (Entity)
     */
    public List<Plugin> findAllActive() {
        return pluginRepository.findByStatus(PluginStatus.ACTIVE);
    }

    /**
     * 활성 플러그인 목록 조회 (DTO)
     * - Entity → DTO 변환하여 민감 정보 제외
     */
    public List<PluginResponse> getActivePlugins() {
        return findAllActive().stream()
                .map(PluginResponse::from)
                .toList();
    }

    /**
     * Plugin Entity를 PluginConfig DTO로 변환
     * - secrets: JSON 파싱 (이미 복호화됨)
     * - metadata: JSON 파싱
     */
    public PluginConfig toPluginConfig(Plugin plugin) {
        Map<String, String> secrets = parseSecrets(plugin.getSecrets());
        Map<String, Object> metadata = parseMetadata(plugin.getMetadata());

        return PluginConfig.builder()
                .pluginId(plugin.getPluginId())
                .clientId(plugin.getClientId())
                .clientSecret(plugin.getClientSecret())
                .secrets(secrets)
                .metadata(metadata)
                .build();
    }

    /**
     * pluginId로 PluginConfig 조회
     */
    public Optional<PluginConfig> getPluginConfig(String pluginId) {
        return findByPluginId(pluginId)
                .map(this::toPluginConfig);
    }

    /**
     * secrets JSON 파싱
     */
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

    /**
     * metadata JSON 파싱
     */
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

    /**
     * 플러그인 저장
     */
    @Transactional
    public Plugin save(Plugin plugin) {
        return pluginRepository.save(plugin);
    }

    /**
     * 플러그인 존재 여부 확인
     */
    public boolean existsByPluginId(String pluginId) {
        return pluginRepository.existsByPluginId(pluginId);
    }
}
