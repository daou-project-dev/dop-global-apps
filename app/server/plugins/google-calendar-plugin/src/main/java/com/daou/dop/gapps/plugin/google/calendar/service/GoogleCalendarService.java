package com.daou.dop.gapps.plugin.google.calendar.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Google Calendar API 서비스
 */
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);

    private final Calendar calendarService;

    public GoogleCalendarService(Calendar calendarService) {
        this.calendarService = calendarService;
    }

    // ==================== Calendar API ====================

    /**
     * 캘린더 목록 조회
     */
    public CalendarList listCalendars() throws IOException {
        log.debug("Listing calendars");
        return calendarService.calendarList().list().execute();
    }

    // ==================== Events API ====================

    /**
     * 일정 목록 조회
     *
     * @param calendarId   캘린더 ID ("primary" 또는 캘린더 이메일)
     * @param timeMin      시작 시간 (ISO 8601)
     * @param timeMax      종료 시간 (ISO 8601)
     * @param maxResults   최대 결과 수
     * @param orderBy      정렬 기준 ("startTime" 또는 "updated")
     * @param singleEvents 반복 일정 확장 여부
     * @param pageToken    페이지 토큰
     */
    public Events listEvents(String calendarId, String timeMin, String timeMax,
                             Integer maxResults, String orderBy, Boolean singleEvents,
                             String pageToken) throws IOException {
        log.debug("Listing events for calendar: {}", calendarId);

        Calendar.Events.List request = calendarService.events().list(calendarId);

        if (timeMin != null) {
            request.setTimeMin(new DateTime(timeMin));
        }
        if (timeMax != null) {
            request.setTimeMax(new DateTime(timeMax));
        }
        if (maxResults != null) {
            request.setMaxResults(maxResults);
        }
        if (orderBy != null) {
            request.setOrderBy(orderBy);
        }
        if (singleEvents != null) {
            request.setSingleEvents(singleEvents);
        }
        if (pageToken != null) {
            request.setPageToken(pageToken);
        }

        return request.execute();
    }

    /**
     * 일정 단건 조회
     *
     * @param calendarId 캘린더 ID
     * @param eventId    일정 ID
     */
    public Event getEvent(String calendarId, String eventId) throws IOException {
        log.debug("Getting event: {} from calendar: {}", eventId, calendarId);
        return calendarService.events().get(calendarId, eventId).execute();
    }

    /**
     * 일정 생성
     *
     * @param calendarId 캘린더 ID
     * @param eventData  일정 데이터
     */
    public Event createEvent(String calendarId, Map<String, Object> eventData) throws IOException {
        log.debug("Creating event in calendar: {}", calendarId);

        Event event = buildEventFromMap(eventData);
        return calendarService.events().insert(calendarId, event).execute();
    }

    /**
     * 일정 수정
     *
     * @param calendarId 캘린더 ID
     * @param eventId    일정 ID
     * @param eventData  수정할 일정 데이터
     */
    public Event updateEvent(String calendarId, String eventId, Map<String, Object> eventData) throws IOException {
        log.debug("Updating event: {} in calendar: {}", eventId, calendarId);

        // 기존 일정 조회
        Event existingEvent = getEvent(calendarId, eventId);

        // 데이터 업데이트
        updateEventFromMap(existingEvent, eventData);

        return calendarService.events().update(calendarId, eventId, existingEvent).execute();
    }

    /**
     * 일정 삭제
     *
     * @param calendarId 캘린더 ID
     * @param eventId    일정 ID
     */
    public void deleteEvent(String calendarId, String eventId) throws IOException {
        log.debug("Deleting event: {} from calendar: {}", eventId, calendarId);
        calendarService.events().delete(calendarId, eventId).execute();
    }

    // ==================== Helper Methods ====================

    /**
     * Map에서 Event 객체 생성
     */
    @SuppressWarnings("unchecked")
    private Event buildEventFromMap(Map<String, Object> data) {
        Event event = new Event();

        if (data.containsKey("summary")) {
            event.setSummary((String) data.get("summary"));
        }
        if (data.containsKey("description")) {
            event.setDescription((String) data.get("description"));
        }
        if (data.containsKey("location")) {
            event.setLocation((String) data.get("location"));
        }

        // 시작 시간
        if (data.containsKey("start")) {
            event.setStart(buildEventDateTime((Map<String, Object>) data.get("start")));
        }

        // 종료 시간
        if (data.containsKey("end")) {
            event.setEnd(buildEventDateTime((Map<String, Object>) data.get("end")));
        }

        // 참석자
        if (data.containsKey("attendees")) {
            List<Map<String, Object>> attendeesList = (List<Map<String, Object>>) data.get("attendees");
            List<EventAttendee> attendees = attendeesList.stream()
                    .map(this::buildAttendee)
                    .toList();
            event.setAttendees(attendees);
        }

        // 반복 규칙
        if (data.containsKey("recurrence")) {
            event.setRecurrence((List<String>) data.get("recurrence"));
        }

        return event;
    }

    /**
     * 기존 Event 객체 업데이트
     */
    @SuppressWarnings("unchecked")
    private void updateEventFromMap(Event event, Map<String, Object> data) {
        if (data.containsKey("summary")) {
            event.setSummary((String) data.get("summary"));
        }
        if (data.containsKey("description")) {
            event.setDescription((String) data.get("description"));
        }
        if (data.containsKey("location")) {
            event.setLocation((String) data.get("location"));
        }
        if (data.containsKey("start")) {
            event.setStart(buildEventDateTime((Map<String, Object>) data.get("start")));
        }
        if (data.containsKey("end")) {
            event.setEnd(buildEventDateTime((Map<String, Object>) data.get("end")));
        }
        if (data.containsKey("attendees")) {
            List<Map<String, Object>> attendeesList = (List<Map<String, Object>>) data.get("attendees");
            List<EventAttendee> attendees = attendeesList.stream()
                    .map(this::buildAttendee)
                    .toList();
            event.setAttendees(attendees);
        }
        if (data.containsKey("recurrence")) {
            event.setRecurrence((List<String>) data.get("recurrence"));
        }
    }

    /**
     * EventDateTime 생성
     */
    private EventDateTime buildEventDateTime(Map<String, Object> data) {
        EventDateTime eventDateTime = new EventDateTime();

        if (data.containsKey("dateTime")) {
            eventDateTime.setDateTime(new DateTime((String) data.get("dateTime")));
        } else if (data.containsKey("date")) {
            // 종일 일정
            eventDateTime.setDate(new DateTime((String) data.get("date")));
        }

        if (data.containsKey("timeZone")) {
            eventDateTime.setTimeZone((String) data.get("timeZone"));
        }

        return eventDateTime;
    }

    /**
     * EventAttendee 생성
     */
    private EventAttendee buildAttendee(Map<String, Object> data) {
        EventAttendee attendee = new EventAttendee();

        if (data.containsKey("email")) {
            attendee.setEmail((String) data.get("email"));
        }
        if (data.containsKey("displayName")) {
            attendee.setDisplayName((String) data.get("displayName"));
        }
        if (data.containsKey("optional")) {
            attendee.setOptional((Boolean) data.get("optional"));
        }
        if (data.containsKey("responseStatus")) {
            attendee.setResponseStatus((String) data.get("responseStatus"));
        }

        return attendee;
    }
}
