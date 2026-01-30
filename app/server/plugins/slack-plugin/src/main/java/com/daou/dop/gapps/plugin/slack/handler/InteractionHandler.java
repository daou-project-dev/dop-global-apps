package com.daou.dop.gapps.plugin.slack.handler;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Slack 인터랙션 핸들러 (버튼, 모달 등)
 */
public class InteractionHandler {

    private static final Logger log = LoggerFactory.getLogger(InteractionHandler.class);

    private InteractionHandler() {
    }

    public static void register(App app) {
        // 버튼 액션 핸들러 (action_id 패턴 매칭)
        app.blockAction("button_click", InteractionHandler::handleButtonClick);

        // 승인/거절 버튼
        app.blockAction("approve_action", InteractionHandler::handleApproveAction);
        app.blockAction("reject_action", InteractionHandler::handleRejectAction);

        log.info("Slack interaction handlers registered");
    }

    private static Response handleButtonClick(BlockActionRequest req, ActionContext ctx) {
        log.info("Button clicked by user: {}", req.getPayload().getUser().getId());

        try {
            ctx.respond("버튼이 클릭되었습니다!");
        } catch (Exception e) {
            log.error("Failed to respond to button click", e);
        }

        return ctx.ack();
    }

    private static Response handleApproveAction(BlockActionRequest req, ActionContext ctx) {
        log.info("Approve action by user: {}", req.getPayload().getUser().getId());

        try {
            ctx.respond("승인되었습니다.");
        } catch (Exception e) {
            log.error("Failed to respond to approve action", e);
        }

        return ctx.ack();
    }

    private static Response handleRejectAction(BlockActionRequest req, ActionContext ctx) {
        log.info("Reject action by user: {}", req.getPayload().getUser().getId());

        try {
            ctx.respond("거절되었습니다.");
        } catch (Exception e) {
            log.error("Failed to respond to reject action", e);
        }

        return ctx.ack();
    }
}
