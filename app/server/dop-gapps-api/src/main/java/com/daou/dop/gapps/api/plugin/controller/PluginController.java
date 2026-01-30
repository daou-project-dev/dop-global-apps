package com.daou.dop.gapps.api.plugin.controller;

import com.daou.dop.gapps.api.plugin.dto.PluginResponse;
import com.daou.dop.gapps.core.plugin.PluginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 플러그인 API
 * - GET /api/plugins: 활성 플러그인 목록 조회
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    private final PluginService pluginService;

    public PluginController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    /**
     * 활성 플러그인 목록 조회
     *
     * @return 플러그인 목록 (민감 정보 제외)
     */
    @GetMapping
    public ResponseEntity<List<PluginResponse>> getPlugins() {
        List<PluginResponse> plugins = pluginService.findAllActivePlugins()
                .stream()
                .map(PluginResponse::from)
                .toList();
        return ResponseEntity.ok(plugins);
    }
}
