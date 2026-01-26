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
 * events.update 핸들러 - 일정 수정
 */
public class EventsUpdateHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(EventsUpdateHandler.class);

    @Override
    public String getAction() {
        return "events.update";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService) {
        String calendarId = request.getStringParam("calendarId");
        String eventId = request.getStringParam("eventId");
        Map<String, Object> eventData = request.getParam("event", Map.class);

        if (calendarId == null || calendarId.isBlank()) {
            return ExecuteResponse.error(400, "calendarId is required");
        }
        if (eventId == null || eventId.isBlank()) {
            return ExecuteResponse.error(400, "eventId is required");
        }
        if (eventData == null || eventData.isEmpty()) {
            return ExecuteResponse.error(400, "event data is required");
        }

        try {
            Event updatedEvent = calendarService.updateEvent(calendarId, eventId, eventData);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedEvent.getId());
            response.put("summary", updatedEvent.getSummary());
            response.put("htmlLink", updatedEvent.getHtmlLink());
            response.put("updated", updatedEvent.getUpdated() != null
                    ? updatedEvent.getUpdated().toStringRfc3339() : null);

            log.info("Updated event: {} in calendar: {}", eventId, calendarId);
            return ExecuteResponse.success(200, gson.toJson(response));

        } catch (IOException e) {
            log.error("Failed to update event", e);
            return ExecuteResponse.error(500, "Failed to update event: " + e.getMessage());
        }
    }
}
