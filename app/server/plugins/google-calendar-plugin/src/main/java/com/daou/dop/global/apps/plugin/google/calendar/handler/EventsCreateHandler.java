package com.daou.dop.global.apps.plugin.google.calendar.handler;

import com.daou.dop.global.apps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * events.create 핸들러 - 일정 생성
 */
public class EventsCreateHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(EventsCreateHandler.class);

    @Override
    public String getAction() {
        return "events.create";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService) {
        String calendarId = request.getStringParam("calendarId");
        Map<String, Object> eventData = request.getParam("event", Map.class);

        if (calendarId == null || calendarId.isBlank()) {
            return ExecuteResponse.error(400, "calendarId is required");
        }
        if (eventData == null || eventData.isEmpty()) {
            return ExecuteResponse.error(400, "event data is required");
        }

        try {
            Event createdEvent = calendarService.createEvent(calendarId, eventData);

            Map<String, Object> response = new HashMap<>();
            response.put("id", createdEvent.getId());
            response.put("summary", createdEvent.getSummary());
            response.put("htmlLink", createdEvent.getHtmlLink());

            log.info("Created event: {} in calendar: {}", createdEvent.getId(), calendarId);
            return ExecuteResponse.success(200, gson.toJson(response));

        } catch (IOException e) {
            log.error("Failed to create event", e);
            return ExecuteResponse.error(500, "Failed to create event: " + e.getMessage());
        }
    }
}
