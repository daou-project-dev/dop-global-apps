package com.daou.dop.gapps.plugin.google.calendar.handler;

import com.daou.dop.gapps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.gapps.plugin.sdk.ExecuteRequest;
import com.daou.dop.gapps.plugin.sdk.ExecuteResponse;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * events.create 핸들러 - 일정 생성
 */
public class EventsCreateHandler extends BaseHandler {

    private static final Logger log = LoggerFactory.getLogger(EventsCreateHandler.class);
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String getAction() {
        return "events.create";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecuteResponse handle(ExecuteRequest request, GoogleCalendarService calendarService) {
        String calendarId = request.getStringParam("calendarId");

        // 플랫한 파라미터 또는 event 객체 지원
        Map<String, Object> eventData = request.getParam("event", Map.class);
        if (eventData == null) {
            eventData = buildEventDataFromFlatParams(request);
        }

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

    /**
     * 플랫한 파라미터에서 event 데이터 구조 생성
     * - startDateTime, endDateTime: "yyyy-MM-dd HH:mm" 형식 → ISO 8601 변환
     */
    private Map<String, Object> buildEventDataFromFlatParams(ExecuteRequest request) {
        String summary = request.getStringParam("summary");
        String description = request.getStringParam("description");
        String startDateTime = request.getStringParam("startDateTime");
        String endDateTime = request.getStringParam("endDateTime");
        String timeZone = request.getStringParam("timeZone");

        if (summary == null || startDateTime == null || endDateTime == null) {
            return null;
        }

        if (timeZone == null || timeZone.isBlank()) {
            timeZone = "Asia/Seoul";
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("summary", summary);

        if (description != null && !description.isBlank()) {
            eventData.put("description", description);
        }

        // 시작/종료 시간 변환
        String startIso = convertToIso8601(startDateTime, timeZone);
        String endIso = convertToIso8601(endDateTime, timeZone);

        if (startIso == null || endIso == null) {
            return null;
        }

        Map<String, Object> start = new HashMap<>();
        start.put("dateTime", startIso);
        start.put("timeZone", timeZone);
        eventData.put("start", start);

        Map<String, Object> end = new HashMap<>();
        end.put("dateTime", endIso);
        end.put("timeZone", timeZone);
        eventData.put("end", end);

        return eventData;
    }

    /**
     * "yyyy-MM-dd HH:mm" → ISO 8601 형식 변환
     */
    private String convertToIso8601(String dateTimeStr, String timeZone) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, INPUT_FORMAT);
            ZoneId zoneId = ZoneId.of(timeZone);
            return localDateTime.atZone(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse dateTime: {}", dateTimeStr, e);
            return null;
        }
    }
}
