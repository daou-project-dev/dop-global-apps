package com.daou.dop.global.apps.plugin.google.calendar.handler;

import com.daou.dop.global.apps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;

/**
 * Google Calendar Action Handler 인터페이스
 */
public interface ActionHandler {

    /**
     * 처리할 action 이름
     */
    String getAction();

    /**
     * action 실행
     *
     * @param request         실행 요청
     * @param calendarService Calendar API 서비스
     * @return 실행 결과
     */
    ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService);
}
