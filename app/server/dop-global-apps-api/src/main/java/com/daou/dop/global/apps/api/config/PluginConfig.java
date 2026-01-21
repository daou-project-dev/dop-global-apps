package com.daou.dop.global.apps.api.config;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PF4J 플러그인 설정
 *
 * - 플러그인 동적 로딩 관리
 * - Extension Point 검색 및 Spring Bean 통합
 */
@Configuration
public class PluginConfig {

    @Value("${pf4j.plugins-dir:./plugins}")
    private String pluginsDir;

    /**
     * SpringPluginManager Bean
     *
     * PF4J와 Spring 통합 관리자
     * - 플러그인 로드/언로드
     * - Extension 자동 수집
     */
    @Bean
    public SpringPluginManager pluginManager() {
        Path pluginsPath = Paths.get(pluginsDir);
        SpringPluginManager manager = new SpringPluginManager(pluginsPath);

        // 플러그인 자동 로드
        manager.loadPlugins();
        manager.startPlugins();

        return manager;
    }
}
