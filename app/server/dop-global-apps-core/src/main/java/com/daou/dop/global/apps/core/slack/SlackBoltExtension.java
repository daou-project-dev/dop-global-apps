package com.daou.dop.global.apps.core.slack;

import com.slack.api.bolt.App;
import org.pf4j.ExtensionPoint;

/**
 * Slack Bolt 핸들러 등록을 위한 ExtensionPoint
 */
public interface SlackBoltExtension extends ExtensionPoint {

    /**
     * Bolt App에 이벤트/커맨드/인터랙션 핸들러 등록
     *
     * @param app Slack Bolt App 인스턴스
     */
    void configureHandlers(App app);

    /**
     * 핸들러 등록 순서 (낮을수록 먼저 실행)
     */
    default int getOrder() {
        return 100;
    }
}
