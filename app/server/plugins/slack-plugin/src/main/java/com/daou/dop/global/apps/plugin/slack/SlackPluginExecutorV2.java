package com.daou.dop.global.apps.plugin.slack;

import com.daou.dop.global.apps.plugin.sdk.CredentialContext;
import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;
import com.daou.dop.global.apps.plugin.sdk.PluginExecutor;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.ConversationType;
import com.slack.api.util.json.GsonFactory;
import com.google.gson.Gson;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Slack API 실행을 위한 PluginExecutor V2 구현
 *
 * <p>V1과의 차이점:
 * <ul>
 *   <li>action 기반 (method/uri 대신)</li>
 *   <li>params Map 사용 (JSON body 대신)</li>
 *   <li>CredentialContext 포함 (accessToken만 아닌 전체 인증 정보)</li>
 * </ul>
 */
@Extension
public class SlackPluginExecutorV2 implements PluginExecutor {

    private static final Logger log = LoggerFactory.getLogger(SlackPluginExecutorV2.class);
    private static final String PLUGIN_ID = "slack";

    private static final List<String> SUPPORTED_ACTIONS = List.of(
            "chat.postMessage",
            "conversations.list",
            "users.list"
    );

    private final Slack slack = Slack.getInstance();
    private final Gson gson = GsonFactory.createSnakeCase();

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public List<String> getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        CredentialContext credential = request.credential();

        // 인증 검증
        if (credential == null || !credential.isOAuth()) {
            return ExecuteResponse.error(401, "Access token required");
        }

        if (credential.isExpired()) {
            return ExecuteResponse.error(401, "Token expired");
        }

        String action = request.action();
        if (action == null || action.isBlank()) {
            return ExecuteResponse.error(400, "Action is required");
        }

        if (!supportsAction(action)) {
            return ExecuteResponse.error(400, "Unsupported action: " + action);
        }

        log.debug("Executing action: {}", action);

        return switch (action) {
            case "chat.postMessage" -> handleChatPostMessage(request, credential);
            case "conversations.list" -> handleConversationsList(request, credential);
            case "users.list" -> handleUsersList(request, credential);
            default -> ExecuteResponse.error(400, "Unsupported action: " + action);
        };
    }

    private ExecuteResponse handleChatPostMessage(ExecuteRequest request, CredentialContext credential) {
        try {
            String channel = request.getStringParam("channel");
            String text = request.getStringParam("text");

            if (channel == null || channel.isBlank()) {
                return ExecuteResponse.error(400, "channel is required");
            }

            MethodsClient methods = slack.methods(credential.accessToken());

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

    private ExecuteResponse handleConversationsList(ExecuteRequest request, CredentialContext credential) {
        try {
            MethodsClient methods = slack.methods(credential.accessToken());

            Integer limit = request.getIntParam("limit");
            Boolean excludeArchived = request.getBooleanParam("excludeArchived");

            ConversationsListRequest listRequest = ConversationsListRequest.builder()
                    .types(List.of(ConversationType.PUBLIC_CHANNEL, ConversationType.PRIVATE_CHANNEL))
                    .excludeArchived(excludeArchived != null ? excludeArchived : true)
                    .limit(limit != null ? limit : 100)
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

    private ExecuteResponse handleUsersList(ExecuteRequest request, CredentialContext credential) {
        try {
            MethodsClient methods = slack.methods(credential.accessToken());

            Integer limit = request.getIntParam("limit");

            UsersListRequest listRequest = UsersListRequest.builder()
                    .limit(limit != null ? limit : 100)
                    .build();

            UsersListResponse response = methods.usersList(listRequest);

            if (response.isOk()) {
                log.info("Fetched {} users", response.getMembers().size());
                return ExecuteResponse.success(200, gson.toJson(response));
            } else {
                log.warn("Failed to list users: {}", response.getError());
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
