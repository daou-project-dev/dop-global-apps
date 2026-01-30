package com.daou.dop.gapps.plugin.google.calendar.handler;

import com.daou.dop.gapps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.gapps.plugin.sdk.ExecuteRequest;
import com.daou.dop.gapps.plugin.sdk.ExecuteResponse;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * events.get 핸들러 - 일정 단건 조회
 */
public class EventsGetHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(EventsGetHandler.class);

    @Override
    public String getAction() {
        return "events.get";
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
            Event event = calendarService.getEvent(calendarId, eventId);

            log.info("Retrieved event: {} from calendar: {}", eventId, calendarId);
            return ExecuteResponse.success(200, gson.toJson(convertEvent(event)));

        } catch (IOException e) {
            log.error("Failed to get event", e);
            return ExecuteResponse.error(500, "Failed to get event: " + e.getMessage());
        }
    }
}
