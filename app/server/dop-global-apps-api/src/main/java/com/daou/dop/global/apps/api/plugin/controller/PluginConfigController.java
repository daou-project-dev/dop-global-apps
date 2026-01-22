package com.daou.dop.global.apps.api.plugin.controller;

import com.daou.dop.global.apps.api.plugin.service.PluginResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 플러그인 폼 설정 API
 * - GET /api/plugins/{pluginId}/form-config: 인증/설정 폼
 * - GET /api/plugins/{pluginId}/test-form: API 테스트 폼
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginConfigController {

    private final PluginResourceService pluginResourceService;

    public PluginConfigController(PluginResourceService pluginResourceService) {
        this.pluginResourceService = pluginResourceService;
    }

    /**
     * 플러그인 폼 설정 조회
     */
    @GetMapping("/{pluginId}/form-config")
    public ResponseEntity<Map<String, Object>> getFormConfig(@PathVariable String pluginId) {
        return pluginResourceService.getFormConfig(pluginId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 플러그인 테스트 폼 조회
     */
    @GetMapping("/{pluginId}/test-form")
    public ResponseEntity<Map<String, Object>> getTestForm(@PathVariable String pluginId) {
        return pluginResourceService.getTestForm(pluginId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
