package com.daou.dop.global.apps.server;

import com.daou.dop.global.apps.core.SimpleExtension;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PluginIntegrationTest {

    @Autowired
    private PluginManager pluginManager;

    @Test
    void testPluginLoadingAndExecution() {
        // 1. 플러그인 로드 확인 (개발 모드에서는 클래스패스 로딩)
        System.out.println("Plugin IDs: " + pluginManager.getPlugins());

        // 2. 확장 기능 검색
        List<SimpleExtension> extensions = pluginManager.getExtensions(SimpleExtension.class);
        assertFalse(extensions.isEmpty(), "Extensions should not be empty");

        // 3. 실행 결과 검증
        SimpleExtension slackExt = extensions.stream()
                .filter(ext -> ext.getClass().getSimpleName().equals("SlackExtension"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("SlackExtension not found"));

        String result = slackExt.execute("Hello");
        assertEquals("Slack: Hello", result);

        System.out.println("Test Passed: " + result);
    }
}
