package com.daou.dop.gapps.core.webhook;

import com.daou.dop.gapps.plugin.sdk.WebhookEvent;

/**
 * 웹훅 이벤트 디스패처 인터페이스
 */
public interface WebhookDispatcher {

    /**
     * 이벤트 디스패치
     *
     * @param event 웹훅 이벤트
     */
    void dispatch(WebhookEvent event);
}
