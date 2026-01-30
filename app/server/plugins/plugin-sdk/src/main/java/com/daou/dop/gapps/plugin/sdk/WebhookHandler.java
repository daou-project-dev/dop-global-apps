package com.daou.dop.gapps.plugin.sdk;

import org.pf4j.ExtensionPoint;

import java.util.Map;
import java.util.Optional;

/**
 * 웹훅 처리를 위한 플러그인 확장점
 * 각 플러그인(Slack, Jira, GitHub 등)에서 구현
 */
public interface WebhookHandler extends ExtensionPoint {

    /**
     * 플러그인 ID
     *
     * @return 플러그인 고유 식별자 (예: "slack", "jira")
     */
    String getPluginId();

    /**
     * 웹훅 서명 검증
     *
     * @param config  플러그인 설정 (secrets.signing_secret 등)
     * @param payload 원본 페이로드 (바이트)
     * @param headers HTTP 헤더
     * @return 검증 성공 여부
     */
    boolean verifySignature(PluginConfig config, byte[] payload, Map<String, String> headers);

    /**
     * 페이로드에서 외부 식별자 추출
     * Connection 조회용 (parseEvent 전에 호출)
     *
     * @param rawPayload 원본 JSON
     * @param headers    HTTP 헤더
     * @return 외부 식별자 (Slack: teamId, Jira: cloudId 등)
     */
    String extractExternalId(String rawPayload, Map<String, String> headers);

    /**
     * 이벤트 파싱
     *
     * @param rawPayload 원본 JSON
     * @param headers    HTTP 헤더
     * @return 파싱된 이벤트
     */
    WebhookEvent parseEvent(String rawPayload, Map<String, String> headers);

    /**
     * 즉시 응답 필요 여부 및 응답 내용
     * URL 검증, challenge 응답 등에 사용
     *
     * @param event      파싱된 이벤트
     * @param rawPayload 원본 페이로드 (challenge 추출용)
     * @return 즉시 응답 필요 시 Optional.of(), 아니면 empty()
     */
    default Optional<WebhookImmediateResponse> getImmediateResponse(WebhookEvent event, String rawPayload) {
        return Optional.empty();
    }

    /**
     * 서명 검증 지원 여부
     * false인 경우 verifySignature 호출 생략
     *
     * @return 서명 검증 지원 여부
     */
    default boolean supportsSignatureVerification() {
        return true;
    }
}
