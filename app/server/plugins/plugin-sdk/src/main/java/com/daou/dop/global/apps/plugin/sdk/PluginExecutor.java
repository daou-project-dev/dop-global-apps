package com.daou.dop.global.apps.plugin.sdk;

import org.pf4j.ExtensionPoint;

import java.util.List;

/**
 * 플러그인 API 실행을 위한 확장점
 */
public interface PluginExecutor extends ExtensionPoint {

    /**
     * 플러그인 ID
     *
     * @return 플러그인 고유 식별자 (예: "slack", "google")
     */
    String getPluginId();

    /**
     * 지원하는 액션 목록
     *
     * @return 지원 액션 리스트 (예: ["chat.postMessage", "conversations.list"])
     */
    List<String> getSupportedActions();

    /**
     * API 실행
     *
     * @param request 실행 요청 (credential 포함)
     * @return 실행 결과
     */
    ExecuteResponse execute(ExecuteRequest request);

    /**
     * 액션 지원 여부 확인
     *
     * @param action 확인할 액션
     * @return 지원 여부
     */
    default boolean supportsAction(String action) {
        return getSupportedActions().contains(action);
    }
}
