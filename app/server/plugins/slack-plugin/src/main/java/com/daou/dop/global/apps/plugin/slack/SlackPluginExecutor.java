package com.daou.dop.global.apps.plugin.slack;

import com.daou.dop.global.apps.core.execute.PluginExecutor;
import com.daou.dop.global.apps.core.execute.dto.ExecuteRequest;
import com.daou.dop.global.apps.core.execute.dto.ExecuteResponse;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.ConversationType;
import com.slack.api.util.json.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Slack API 실행을 위한 PluginExecutor 구현체
 */
@Extension
public class SlackPluginExecutor implements PluginExecutor {

    private static final Logger log = LoggerFactory.getLogger(SlackPluginExecutor.class);
    private static final String PLUGIN_NAME = "slack";

    private final Slack slack = Slack.getInstance();
    private final Gson gson = GsonFactory.createSnakeCase();

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        if (request.accessToken() == null || request.accessToken().isBlank()) {
            return ExecuteResponse.error(401, "Access token is required");
        }

        String uri = request.uri();
        if (uri == null || uri.isBlank()) {
            return ExecuteResponse.error(400, "URI is required");
        }

        // URI 정규화 (앞의 슬래시 제거)
        String normalizedUri = uri.startsWith("/") ? uri.substring(1) : uri;

        return switch (normalizedUri) {
            case "chat.postMessage" -> handleChatPostMessage(request);
            case "conversations.list" -> handleConversationsList(request);
            default -> ExecuteResponse.error(400, "Unsupported API: " + normalizedUri);
        };
    }

    private ExecuteResponse handleChatPostMessage(ExecuteRequest request) {
        try {
            JsonObject body = JsonParser.parseString(request.body()).getAsJsonObject();

            String channel = body.has("channel") ? body.get("channel").getAsString() : null;
            String text = body.has("text") ? body.get("text").getAsString() : null;

            if (channel == null || channel.isBlank()) {
                return ExecuteResponse.error(400, "channel is required");
            }

            MethodsClient methods = slack.methods(request.accessToken());

            ChatPostMessageRequest chatRequest = ChatPostMessageRequest.builder()
                    .channel(channel)
                    .text(text)
                    .build();

            ChatPostMessageResponse response = methods.chatPostMessage(chatRequest);

            if (response.isOk()) {
                log.info("Message sent to channel: {}", channel);
                return ExecuteResponse.success(200, gson.toJson(response));
            } else {
                log.warn("Failed to send message: {}", response.getError());
                return ExecuteResponse.error(400, response.getError());
            }

        } catch (IOException | SlackApiException e) {
            log.error("Slack API error", e);
            return ExecuteResponse.error("Slack API error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ExecuteResponse.error("Unexpected error: " + e.getMessage());
        }
    }

    private ExecuteResponse handleConversationsList(ExecuteRequest request) {
        try {
            MethodsClient methods = slack.methods(request.accessToken());

            ConversationsListRequest listRequest = ConversationsListRequest.builder()
                    .types(List.of(ConversationType.PUBLIC_CHANNEL))
                    .excludeArchived(true)
                    .limit(100)
                    .build();

            ConversationsListResponse response = methods.conversationsList(listRequest);

            if (response.isOk()) {
                log.info("Fetched {} channels", response.getChannels().size());
                return ExecuteResponse.success(200, gson.toJson(response));
            } else {
                log.warn("Failed to list conversations: {}", response.getError());
                return ExecuteResponse.error(400, response.getError());
            }

        } catch (IOException | SlackApiException e) {
            log.error("Slack API error", e);
            return ExecuteResponse.error("Slack API error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ExecuteResponse.error("Unexpected error: " + e.getMessage());
        }
    }
}
