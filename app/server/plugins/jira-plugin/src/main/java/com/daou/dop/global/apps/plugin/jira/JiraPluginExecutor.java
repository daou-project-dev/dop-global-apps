package com.daou.dop.global.apps.plugin.jira;

import com.daou.dop.global.apps.plugin.sdk.CredentialContext;
import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;
import com.daou.dop.global.apps.plugin.sdk.PluginExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Jira API 실행을 위한 PluginExecutor 구현
 */
@Extension
public class JiraPluginExecutor implements PluginExecutor {

    private static final Logger log = LoggerFactory.getLogger(JiraPluginExecutor.class);
    private static final String PLUGIN_ID = "jira";

    private static final List<String> SUPPORTED_ACTIONS = List.of(
            "myself",
            "search",
            "project"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JiraPluginExecutor() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

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

        // metadata에서 cloudId 추출
        Map<String, String> metadata = credential.metadata();
        String cloudId = metadata != null ? metadata.get("cloudId") : null;
        if (cloudId == null || cloudId.isBlank()) {
            return ExecuteResponse.error(400, "cloudId not found in credential metadata");
        }

        log.debug("Executing action: {} for cloudId: {}", action, cloudId);

        return switch (action) {
            case "myself" -> handleMyself(credential, cloudId);
            case "search" -> handleSearch(request, credential, cloudId);
            case "project" -> handleProject(credential, cloudId);
            default -> ExecuteResponse.error(400, "Unsupported action: " + action);
        };
    }

    /**
     * 현재 사용자 정보 조회
     */
    private ExecuteResponse handleMyself(CredentialContext credential, String cloudId) {
        String url = String.format("https://api.atlassian.com/ex/jira/%s/rest/api/3/myself", cloudId);
        return callJiraApi("GET", url, null, credential.accessToken());
    }

    /**
     * 이슈 검색 (JQL)
     * API 변경: /rest/api/3/search → /rest/api/3/search/jql (POST)
     * 참고: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-search/#api-rest-api-3-search-jql-post
     */
    private ExecuteResponse handleSearch(ExecuteRequest request, CredentialContext credential, String cloudId) {
        String jql = request.getStringParam("jql");
        Integer maxResults = request.getIntParam("maxResults");

        // JQL 필수 - 없으면 기본값 사용 (내 이슈)
        if (jql == null || jql.isBlank()) {
            jql = "assignee = currentUser() order by created DESC";
        }

        String url = String.format("https://api.atlassian.com/ex/jira/%s/rest/api/3/search/jql", cloudId);

        // POST body 구성
        StringBuilder bodyBuilder = new StringBuilder("{");
        bodyBuilder.append("\"jql\":\"").append(escapeJson(jql)).append("\"");
        if (maxResults != null) {
            bodyBuilder.append(",\"maxResults\":").append(maxResults);
        }
        bodyBuilder.append("}");

        return callJiraApi("POST", url, bodyBuilder.toString(), credential.accessToken());
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * 프로젝트 목록 조회
     */
    private ExecuteResponse handleProject(CredentialContext credential, String cloudId) {
        String url = String.format("https://api.atlassian.com/ex/jira/%s/rest/api/3/project", cloudId);
        return callJiraApi("GET", url, null, credential.accessToken());
    }

    /**
     * Jira API 호출
     */
    private ExecuteResponse callJiraApi(String method, String url, String body, String accessToken) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30));

            if ("GET".equals(method)) {
                requestBuilder.GET();
            } else if ("POST".equals(method)) {
                requestBuilder.header("Content-Type", "application/json");
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.debug("Jira API success: {}", url);
                return ExecuteResponse.success(response.statusCode(), response.body());
            } else {
                log.warn("Jira API error: {} - {}", response.statusCode(), response.body());
                return ExecuteResponse.error(response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Jira API call failed: {}", url, e);
            return ExecuteResponse.error("Jira API error: " + e.getMessage());
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
