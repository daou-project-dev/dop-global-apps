package com.daou.dop.gapps.plugin.slack;

import com.daou.dop.gapps.plugin.slack.handler.CommandHandler;
import com.daou.dop.gapps.plugin.slack.handler.EventHandler;
import com.daou.dop.gapps.plugin.slack.handler.InteractionHandler;
import com.slack.api.bolt.App;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Slack 이벤트 핸들러
 * Slack Bolt App에 이벤트/커맨드/인터랙션 핸들러 등록
 */
@Extension
public class SlackEventHandler implements ExtensionPoint {

    private static final Logger log = LoggerFactory.getLogger(SlackEventHandler.class);

    /**
     * Slack Bolt App에 핸들러 등록
     */
    public void configureHandlers(App app) {
        log.info("Configuring Slack event handlers");

        // 이벤트 핸들러 등록
        EventHandler.register(app);

        // 커맨드 핸들러 등록
        CommandHandler.register(app);

        // 인터랙션 핸들러 등록
        InteractionHandler.register(app);

        log.info("Slack event handlers configured");
    }

    /**
     * 핸들러 우선순위
     */
    public int getOrder() {
        return 10;
    }
}
