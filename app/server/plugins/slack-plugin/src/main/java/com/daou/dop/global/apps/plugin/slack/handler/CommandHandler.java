package com.daou.dop.global.apps.plugin.slack.handler;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Slack 슬래시 커맨드 핸들러
 */
public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private CommandHandler() {
    }

    public static void register(App app) {
        // /hello 커맨드
        app.command("/hello", CommandHandler::handleHelloCommand);

        // /help 커맨드
        app.command("/help", CommandHandler::handleHelpCommand);

        log.info("Slack command handlers registered");
    }

    private static Response handleHelloCommand(SlashCommandRequest req, SlashCommandContext ctx) {
        log.info("Hello command from user: {}", req.getPayload().getUserId());

        String text = req.getPayload().getText();
        String response = text.isEmpty()
                ? "안녕하세요!"
                : "안녕하세요, " + text + "!";

        return ctx.ack(response);
    }

    private static Response handleHelpCommand(SlashCommandRequest req, SlashCommandContext ctx) {
        log.info("Help command from user: {}", req.getPayload().getUserId());

        String helpText = """
                *사용 가능한 명령어:*
                • `/hello [이름]` - 인사하기
                • `/help` - 도움말 보기
                """;

        return ctx.ack(helpText);
    }
}
