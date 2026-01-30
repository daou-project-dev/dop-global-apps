package com.daou.dop.gapps.plugin.jira;

import com.daou.dop.gapps.plugin.sdk.PluginConfig;
import com.daou.dop.gapps.plugin.sdk.WebhookEvent;
import com.daou.dop.gapps.plugin.sdk.WebhookHandler;
import com.daou.dop.gapps.plugin.sdk.WebhookImmediateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Jira 웹훅 핸들러
 *
 * <p>Jira Cloud 웹훅 처리
 * <p>서명 검증: HMAC-SHA256 (선택적, webhook_secret 설정 시)
 * <p>연동 식별: cloudId 또는 baseUrl
 */
@Extension
public class JiraWebhookHandler implements WebhookHandler {

    private static final Logger log = LoggerFactory.getLogger(JiraWebhookHandler.class);
    private static final String PLUGIN_ID = "jira";
    private static final String SIGNATURE_HEADER = "x-hub-signature";

    private final ObjectMapper objectMapper;

    public JiraWebhookHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public boolean verifySignature(PluginConfig config, byte[] payload, Map<String, String> headers) {
        String secret = config.getSecret("webhook_secret");

        // webhook_secret이 설정되지 않으면 검증 스킵
        if (secret == null || secret.isBlank()) {
            log.debug("Webhook secret not configured, skipping signature verification");
            return true;
        }

        String signature = headers.get(SIGNATURE_HEADER);
        if (signature == null || !signature.startsWith("sha256=")) {
            log.warn("Missing or invalid signature header");
            return false;
        }

        try {
            String expected = "sha256=" + hmacSha256(secret, new String(payload, StandardCharsets.UTF_8));
            boolean valid = MessageDigest.isEqual(
                    signature.getBytes(StandardCharsets.UTF_8),
                    expected.getBytes(StandardCharsets.UTF_8)
            );

            if (!valid) {
                log.warn("Signature verification failed");
            }

            return valid;
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }

    @Override
    public boolean supportsSignatureVerification() {
        return true;
    }

    @Override
    public String extractExternalId(String rawPayload, Map<String, String> headers) {
        try {
            JsonNode json = objectMapper.readTree(rawPayload);

            // 1. baseUrl에서 cloudId 추출 시도
            // 예: https://your-domain.atlassian.net -> your-domain
            String baseUrl = getTextValue(json, "baseUrl");
            if (baseUrl != null && !baseUrl.isBlank()) {
                // https://xxx.atlassian.net 에서 xxx 추출
                String domain = baseUrl.replace("https://", "")
                        .replace("http://", "")
                        .split("\\.")[0];
                log.debug("Extracted externalId from baseUrl: {}", domain);
                return domain;
            }

            // 2. issue.self에서 추출 시도
            JsonNode issue = json.path("issue");
            if (!issue.isMissingNode()) {
                String self = getTextValue(issue, "self");
                if (self != null) {
                    // https://xxx.atlassian.net/rest/api/... 에서 xxx 추출
                    String domain = self.replace("https://", "")
                            .replace("http://", "")
                            .split("\\.")[0];
                    log.debug("Extracted externalId from issue.self: {}", domain);
                    return domain;
                }
            }

            log.warn("Could not extract externalId from Jira webhook payload");
            return null;

        } catch (Exception e) {
            log.error("Error extracting externalId", e);
            return null;
        }
    }

    @Override
    public WebhookEvent parseEvent(String rawPayload, Map<String, String> headers) {
        try {
            JsonNode json = objectMapper.readTree(rawPayload);

            // 이벤트 타입: webhookEvent 필드
            String eventType = getTextValue(json, "webhookEvent");
            if (eventType == null) {
                eventType = "unknown";
            }

            // 외부 ID
            String externalId = extractExternalId(rawPayload, headers);

            // 사용자 ID
            String externalUserId = null;
            JsonNode user = json.path("user");
            if (!user.isMissingNode()) {
                externalUserId = getTextValue(user, "accountId");
            }

            // 타임스탬프
            Instant timestamp = Instant.now();
            String timestampStr = getTextValue(json, "timestamp");
            if (timestampStr != null) {
                try {
                    timestamp = Instant.ofEpochMilli(Long.parseLong(timestampStr));
                } catch (NumberFormatException e) {
                    // 기본값 사용
                }
            }

            // 데이터
            Map<String, Object> data = new HashMap<>();
            data.put("webhookEvent", eventType);

            // issue 정보
            JsonNode issue = json.path("issue");
            if (!issue.isMissingNode()) {
                Map<String, Object> issueData = new HashMap<>();
                issueData.put("id", getTextValue(issue, "id"));
                issueData.put("key", getTextValue(issue, "key"));
                issueData.put("self", getTextValue(issue, "self"));

                JsonNode fields = issue.path("fields");
                if (!fields.isMissingNode()) {
                    issueData.put("summary", getTextValue(fields, "summary"));
                    issueData.put("status", getTextValue(fields.path("status"), "name"));
                    issueData.put("issueType", getTextValue(fields.path("issuetype"), "name"));

                    JsonNode project = fields.path("project");
                    if (!project.isMissingNode()) {
                        issueData.put("projectId", getTextValue(project, "id"));
                        issueData.put("projectKey", getTextValue(project, "key"));
                        issueData.put("projectName", getTextValue(project, "name"));
                    }
                }

                data.put("issue", issueData);
            }

            // changelog 정보 (이슈 업데이트 시)
            JsonNode changelog = json.path("changelog");
            if (!changelog.isMissingNode()) {
                data.put("changelog", objectMapper.convertValue(changelog, Map.class));
            }

            log.debug("Parsed Jira webhook event: type={}, externalId={}", eventType, externalId);

            return new WebhookEvent(
                    PLUGIN_ID,
                    eventType,
                    externalId,
                    externalUserId,
                    timestamp,
                    data
            );

        } catch (Exception e) {
            log.error("Error parsing Jira webhook payload", e);
            return new WebhookEvent(
                    PLUGIN_ID,
                    "parse_error",
                    null,
                    null,
                    Instant.now(),
                    Map.of("error", e.getMessage())
            );
        }
    }

    @Override
    public Optional<WebhookImmediateResponse> getImmediateResponse(WebhookEvent event, String rawPayload) {
        // Jira는 특별한 즉시 응답이 필요 없음
        return Optional.empty();
    }

    private String hmacSha256(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() || field.isNull() ? null : field.asText();
    }
}
