package com.daou.dop.global.apps.plugin.slack.handler;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.model.event.AppMentionEvent;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Slack 이벤트 핸들러
 */
public class EventHandler {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

    private EventHandler() {
    }

    public static void register(App app) {
        // 앱 멘션 이벤트
        app.event(AppMentionEvent.class, (payload, ctx) -> handleAppMention(payload.getEvent(), ctx));

        // 메시지 이벤트 (DM 등)
        app.event(MessageEvent.class, (payload, ctx) -> handleMessage(payload.getEvent(), ctx));

        log.info("Slack event handlers registered");
    }

    private static Response handleAppMention(AppMentionEvent event, EventContext ctx) {
        log.info("App mentioned by user: {} in channel: {}", event.getUser(), event.getChannel());

        try {
            ctx.say("안녕하세요! 무엇을 도와드릴까요?");
        } catch (Exception e) {
            log.error("Failed to respond to app mention", e);
        }

        return ctx.ack();
    }

    private static Response handleMessage(MessageEvent event, EventContext ctx) {
        // 봇 메시지 무시
        if (event.getBotId() != null) {
            return ctx.ack();
        }

        log.debug("Message received from user: {} - {}", event.getUser(), event.getText());
        return ctx.ack();
    }
}
