package com.daou.dop.gapps.core.plugin;

import com.daou.dop.gapps.core.dto.AuthConfigInfo;
import com.daou.dop.gapps.core.dto.PluginConfigInfo;
import com.daou.dop.gapps.core.dto.PluginInfo;
import com.daou.dop.gapps.domain.enums.AuthType;
import com.daou.dop.gapps.core.repository.PluginRepository;
import com.daou.dop.gapps.domain.enums.PluginStatus;
import com.daou.dop.gapps.domain.plugin.Plugin;
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
     * 활성 플러그인 목록 조회 (표시용)
     */
    public List<PluginInfo> findAllActivePlugins() {
        return pluginRepository.findByStatus(PluginStatus.ACTIVE)
                .stream()
                .map(this::toPluginInfo)
                .toList();
    }

    /**
     * 플러그인 존재 여부 확인
     */
    public boolean existsByPluginId(String pluginId) {
        return pluginRepository.existsByPluginId(pluginId);
    }

    // ========== 내부 메서드 ==========

    private PluginInfo toPluginInfo(Plugin plugin) {
        return PluginInfo.builder()
                .pluginId(plugin.getPluginId())
                .name(plugin.getName())
                .description(plugin.getDescription())
                .iconUrl(plugin.getIconUrl())
                .authType(plugin.getAuthType() != null ? plugin.getAuthType().name() : null)
                .active(plugin.getStatus() == PluginStatus.ACTIVE)
                .authConfig(buildAuthConfig(plugin.getPluginId(), plugin.getAuthType()))
                .build();
    }

    private AuthConfigInfo buildAuthConfig(String pluginId, AuthType authType) {
        if (authType == null) {
            return null;
        }

        return switch (authType) {
            case OAUTH2 -> AuthConfigInfo.redirect("/oauth/" + pluginId + "/install");
            case API_KEY -> AuthConfigInfo.submit("/key/" + pluginId + "/credentials");
            case SERVICE_ACCOUNT -> null; // 미지원
        };
    }

    private PluginConfigInfo toPluginConfigInfo(Plugin plugin) {
        Map<String, String> secrets = parseSecrets(plugin.getSecrets());
        Map<String, Object> metadata = parseMetadata(plugin.getMetadata());

        return PluginConfigInfo.builder()
                .pluginId(plugin.getPluginId())
                .displayName(plugin.getName())
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
