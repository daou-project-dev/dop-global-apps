package com.daou.dop.gapps.plugin.google.calendar.handler;

import com.daou.dop.gapps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.gapps.plugin.sdk.ExecuteRequest;
import com.daou.dop.gapps.plugin.sdk.ExecuteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * events.delete 핸들러 - 일정 삭제
 */
public class EventsDeleteHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(EventsDeleteHandler.class);

    @Override
    public String getAction() {
        return "events.delete";
    }

    @Override
    public ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService) {
        String calendarId = request.getStringParam("calendarId");
        String eventId = request.getStringParam("eventId");

        if (calendarId == null || calendarId.isBlank()) {
            return ExecuteResponse.error(400, "calendarId is required");
        }
        if (eventId == null || eventId.isBlank()) {
            return ExecuteResponse.error(400, "eventId is required");
        }

        try {
            calendarService.deleteEvent(calendarId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("deleted", true);
            response.put("eventId", eventId);

            log.info("Deleted event: {} from calendar: {}", eventId, calendarId);
            return ExecuteResponse.success(200, gson.toJson(response));

        } catch (IOException e) {
            log.error("Failed to delete event", e);
            return ExecuteResponse.error(500, "Failed to delete event: " + e.getMessage());
        }
    }
}
