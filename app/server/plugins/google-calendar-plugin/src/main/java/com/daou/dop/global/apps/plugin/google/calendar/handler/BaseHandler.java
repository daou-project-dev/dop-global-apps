package com.daou.dop.global.apps.plugin.google.calendar.handler;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler 공통 기능 제공
 */
public abstract class BaseHandler implements ActionHandler {

    protected final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    /**
     * CalendarListEntry → Map 변환
     */
    protected Map<String, Object> convertCalendarEntry(CalendarListEntry entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entry.getId());
        map.put("summary", entry.getSummary());
        map.put("description", entry.getDescription());
        map.put("primary", entry.getPrimary());
        map.put("accessRole", entry.getAccessRole());
        map.put("timeZone", entry.getTimeZone());
        return map;
    }

    /**
     * Event → Map 변환
     */
    protected Map<String, Object> convertEvent(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.getId());
        map.put("summary", event.getSummary());
        map.put("description", event.getDescription());
        map.put("location", event.getLocation());
        map.put("htmlLink", event.getHtmlLink());
        map.put("status", event.getStatus());

        if (event.getStart() != null) {
            Map<String, Object> start = new HashMap<>();
            if (event.getStart().getDateTime() != null) {
                start.put("dateTime", event.getStart().getDateTime().toStringRfc3339());
            }
            if (event.getStart().getDate() != null) {
                start.put("date", event.getStart().getDate().toStringRfc3339());
            }
            start.put("timeZone", event.getStart().getTimeZone());
            map.put("start", start);
        }

        if (event.getEnd() != null) {
            Map<String, Object> end = new HashMap<>();
            if (event.getEnd().getDateTime() != null) {
                end.put("dateTime", event.getEnd().getDateTime().toStringRfc3339());
            }
            if (event.getEnd().getDate() != null) {
                end.put("date", event.getEnd().getDate().toStringRfc3339());
            }
            end.put("timeZone", event.getEnd().getTimeZone());
            map.put("end", end);
        }

        if (event.getAttendees() != null) {
            List<Map<String, Object>> attendees = event.getAttendees().stream()
                    .map(a -> {
                        Map<String, Object> attendee = new HashMap<>();
                        attendee.put("email", a.getEmail());
                        attendee.put("displayName", a.getDisplayName());
                        attendee.put("responseStatus", a.getResponseStatus());
                        attendee.put("optional", a.getOptional());
                        attendee.put("organizer", a.getOrganizer());
                        return attendee;
                    })
                    .toList();
            map.put("attendees", attendees);
        }

        if (event.getCreator() != null) {
            Map<String, Object> creator = new HashMap<>();
            creator.put("email", event.getCreator().getEmail());
            creator.put("displayName", event.getCreator().getDisplayName());
            map.put("creator", creator);
        }

        if (event.getOrganizer() != null) {
            Map<String, Object> organizer = new HashMap<>();
            organizer.put("email", event.getOrganizer().getEmail());
            organizer.put("displayName", event.getOrganizer().getDisplayName());
            map.put("organizer", organizer);
        }

        return map;
    }
}
