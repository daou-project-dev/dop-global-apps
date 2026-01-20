package com.daou.dop.global.apps.plugin.slack;

import com.daou.dop.global.apps.core.slack.SlackBoltExtension;
import com.daou.dop.global.apps.plugin.slack.handler.CommandHandler;
import com.daou.dop.global.apps.plugin.slack.handler.EventHandler;
import com.daou.dop.global.apps.plugin.slack.handler.InteractionHandler;
import com.slack.api.bolt.App;
import org.pf4j.Extension;

/**
 * Slack Bolt 핸들러 등록 구현체
 */
@Extension
public class SlackBoltExtensionImpl implements SlackBoltExtension {

    @Override
    public void configureHandlers(App app) {
        // 이벤트 핸들러 등록
        EventHandler.register(app);

        // 커맨드 핸들러 등록
        CommandHandler.register(app);

        // 인터랙션 핸들러 등록
        InteractionHandler.register(app);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
