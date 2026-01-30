package com.daou.dop.gapps.plugin.google.calendar.handler;

import com.daou.dop.gapps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.gapps.plugin.sdk.ExecuteRequest;
import com.daou.dop.gapps.plugin.sdk.ExecuteResponse;
import com.google.api.services.calendar.model.CalendarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * calendar.list 핸들러 - 캘린더 목록 조회
 */
public class CalendarListHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(CalendarListHandler.class);

    @Override
    public String getAction() {
        return "calendar.list";
    }

    @Override
    public ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService) {
        try {
            CalendarList calendarList = calendarService.listCalendars();

            Map<String, Object> response = new HashMap<>();
            List<Map<String, Object>> calendars = calendarList.getItems().stream()
                    .map(this::convertCalendarEntry)
                    .toList();
            response.put("calendars", calendars);

            log.info("Listed {} calendars", calendars.size());
            return ExecuteResponse.success(200, gson.toJson(response));

        } catch (IOException e) {
            log.error("Failed to list calendars", e);
            return ExecuteResponse.error(500, "Failed to list calendars: " + e.getMessage());
        }
    }
}
