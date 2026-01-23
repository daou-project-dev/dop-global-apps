package com.daou.dop.global.apps.plugin.google.calendar.handler;

import com.daou.dop.global.apps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;
import com.google.api.services.calendar.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * events.list 핸들러 - 일정 목록 조회
 */
public class EventsListHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(EventsListHandler.class);

    @Override
    public String getAction() {
        return "events.list";
    }

    @Override
    public ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService) {
        String calendarId = request.getStringParam("calendarId");
        if (calendarId == null || calendarId.isBlank()) {
            return ExecuteResponse.error(400, "calendarId is required");
        }

        String timeMin = request.getStringParam("timeMin");
        String timeMax = request.getStringParam("timeMax");
        Integer maxResults = request.getIntParam("maxResults");
        String orderBy = request.getStringParam("orderBy");
        Boolean singleEvents = request.getBooleanParam("singleEvents");
        String pageToken = request.getStringParam("pageToken");

        try {
            Events events = calendarService.listEvents(
                    calendarId, timeMin, timeMax, maxResults, orderBy, singleEvents, pageToken
            );

            Map<String, Object> response = new HashMap<>();
            List<Map<String, Object>> eventList = events.getItems().stream()
                    .map(this::convertEvent)
                    .toList();
            response.put("events", eventList);

            if (events.getNextPageToken() != null) {
                response.put("nextPageToken", events.getNextPageToken());
            }

            log.info("Listed {} events for calendar: {}", eventList.size(), calendarId);
            return ExecuteResponse.success(200, gson.toJson(response));

        } catch (IOException e) {
            log.error("Failed to list events", e);
            return ExecuteResponse.error(500, "Failed to list events: " + e.getMessage());
        }
    }
}
