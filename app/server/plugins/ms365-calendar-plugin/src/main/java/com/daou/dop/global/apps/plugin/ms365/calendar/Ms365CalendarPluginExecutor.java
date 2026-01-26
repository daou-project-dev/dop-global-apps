package com.daou.dop.global.apps.plugin.ms365.calendar;

import com.daou.dop.global.apps.plugin.ms365.calendar.client.GraphApiClient;
import com.daou.dop.global.apps.plugin.ms365.calendar.client.GraphApiClient.GraphApiResponse;
import com.daou.dop.global.apps.plugin.ms365.calendar.dto.CreateEventRequest;
import com.daou.dop.global.apps.plugin.sdk.CredentialContext;
import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;
import com.daou.dop.global.apps.plugin.sdk.PluginExecutor;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Microsoft 365 Calendar API 실행을 위한 PluginExecutor 구현
 */
@Extension
public class Ms365CalendarPluginExecutor implements PluginExecutor {

    private static final Logger log = LoggerFactory.getLogger(Ms365CalendarPluginExecutor.class);
    private static final String PLUGIN_ID = "ms365-calendar";

    private static final List<String> SUPPORTED_ACTIONS = List.of(
            "me.get",
            "calendars.list",
            "events.list",
            "events.create",
            "events.delete"
    );

    private final GraphApiClient graphApiClient;

    public Ms365CalendarPluginExecutor() {
        this.graphApiClient = new GraphApiClient();
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public List<String> getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        CredentialContext credential = request.credential();

        // 인증 검증
        if (credential == null || !credential.isOAuth()) {
            return ExecuteResponse.error(401, "Access token required");
        }

        if (credential.isExpired()) {
            return ExecuteResponse.error(401, "Token expired");
        }

        String action = request.action();
        if (action == null || action.isBlank()) {
            return ExecuteResponse.error(400, "Action is required");
        }

        if (!supportsAction(action)) {
            return ExecuteResponse.error(400, "Unsupported action: " + action);
        }

        log.debug("Executing action: {}", action);

        return switch (action) {
            case "me.get" -> handleGetMe(credential);
            case "calendars.list" -> handleCalendarsList(credential);
            case "events.list" -> handleEventsList(request, credential);
            case "events.create" -> handleEventsCreate(request, credential);
            case "events.delete" -> handleEventsDelete(request, credential);
            default -> ExecuteResponse.error(400, "Unsupported action: " + action);
        };
    }

    /**
     * GET /me - 현재 사용자 프로필 조회
     */
    private ExecuteResponse handleGetMe(CredentialContext credential) {
        try {
            GraphApiResponse response = graphApiClient.get("/me", credential.accessToken());

            if (response.isSuccessful()) {
                log.info("Successfully fetched user profile");
                return ExecuteResponse.success(response.statusCode(), response.body());
            } else {
                log.warn("Failed to fetch user profile: {}", response.body());
                return ExecuteResponse.error(response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to get user profile", e);
            return ExecuteResponse.error("Failed to get user profile: " + e.getMessage());
        }
    }

    /**
     * GET /me/calendars - 캘린더 목록 조회
     */
    private ExecuteResponse handleCalendarsList(CredentialContext credential) {
        try {
            GraphApiResponse response = graphApiClient.get("/me/calendars", credential.accessToken());

            if (response.isSuccessful()) {
                log.info("Successfully fetched calendars list");
                return ExecuteResponse.success(response.statusCode(), response.body());
            } else {
                log.warn("Failed to fetch calendars: {}", response.body());
                return ExecuteResponse.error(response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to list calendars", e);
            return ExecuteResponse.error("Failed to list calendars: " + e.getMessage());
        }
    }

    /**
     * GET /me/calendars/{calendarId}/events - 이벤트 목록 조회
     */
    private ExecuteResponse handleEventsList(ExecuteRequest request, CredentialContext credential) {
        try {
            String calendarId = request.getStringParam("calendarId");
            Integer top = request.getIntParam("top");
            String filter = request.getStringParam("filter");
            String orderBy = request.getStringParam("orderBy");
            String select = request.getStringParam("select");

            // calendarId가 없으면 기본 캘린더 사용
            String path = calendarId != null && !calendarId.isBlank()
                    ? "/me/calendars/" + calendarId + "/events"
                    : "/me/events";

            // 쿼리 파라미터 추가
            StringBuilder queryParams = new StringBuilder();
            List<String> params = new ArrayList<>();

            if (top != null) {
                params.add("$top=" + top);
            }
            if (filter != null && !filter.isBlank()) {
                params.add("$filter=" + filter);
            }
            if (orderBy != null && !orderBy.isBlank()) {
                params.add("$orderby=" + orderBy);
            }
            if (select != null && !select.isBlank()) {
                params.add("$select=" + select);
            }

            if (!params.isEmpty()) {
                queryParams.append("?").append(String.join("&", params));
            }

            GraphApiResponse response = graphApiClient.get(path + queryParams, credential.accessToken());

            if (response.isSuccessful()) {
                log.info("Successfully fetched events list");
                return ExecuteResponse.success(response.statusCode(), response.body());
            } else {
                log.warn("Failed to fetch events: {}", response.body());
                return ExecuteResponse.error(response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to list events", e);
            return ExecuteResponse.error("Failed to list events: " + e.getMessage());
        }
    }

    /**
     * POST /me/calendars/{calendarId}/events - 이벤트 생성
     */
    private ExecuteResponse handleEventsCreate(ExecuteRequest request, CredentialContext credential) {
        try {
            String calendarId = request.getStringParam("calendarId");
            String subject = request.getStringParam("subject");
            String body = request.getStringParam("body");
            String startDateTime = request.getStringParam("startDateTime");
            String endDateTime = request.getStringParam("endDateTime");
            String timeZone = request.getStringParam("timeZone");
            String location = request.getStringParam("location");
            Boolean isAllDay = request.getBooleanParam("isAllDay");
            Boolean isOnlineMeeting = request.getBooleanParam("isOnlineMeeting");

            // 필수 파라미터 검증
            if (subject == null || subject.isBlank()) {
                return ExecuteResponse.error(400, "subject is required");
            }
            if (startDateTime == null || startDateTime.isBlank()) {
                return ExecuteResponse.error(400, "startDateTime is required");
            }
            if (endDateTime == null || endDateTime.isBlank()) {
                return ExecuteResponse.error(400, "endDateTime is required");
            }

            // 기본 타임존
            String tz = timeZone != null && !timeZone.isBlank() ? timeZone : "Asia/Seoul";

            // 이벤트 생성 요청 빌드
            CreateEventRequest.Builder eventBuilder = CreateEventRequest.builder()
                    .subject(subject)
                    .start(startDateTime, tz)
                    .end(endDateTime, tz);

            if (body != null && !body.isBlank()) {
                eventBuilder.body("HTML", body);
            }
            if (location != null && !location.isBlank()) {
                eventBuilder.location(location);
            }
            if (isAllDay != null) {
                eventBuilder.isAllDay(isAllDay);
            }
            if (isOnlineMeeting != null && isOnlineMeeting) {
                eventBuilder.isOnlineMeeting(true);
                eventBuilder.onlineMeetingProvider("teamsForBusiness");
            }

            CreateEventRequest eventRequest = eventBuilder.build();

            // calendarId가 없으면 기본 캘린더 사용
            String path = calendarId != null && !calendarId.isBlank()
                    ? "/me/calendars/" + calendarId + "/events"
                    : "/me/events";

            GraphApiResponse response = graphApiClient.post(path, credential.accessToken(), eventRequest);

            if (response.isSuccessful()) {
                log.info("Successfully created event: {}", subject);
                return ExecuteResponse.success(response.statusCode(), response.body());
            } else {
                log.warn("Failed to create event: {}", response.body());
                return ExecuteResponse.error(response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to create event", e);
            return ExecuteResponse.error("Failed to create event: " + e.getMessage());
        }
    }

    /**
     * DELETE /me/events/{eventId} - 이벤트 삭제
     */
    private ExecuteResponse handleEventsDelete(ExecuteRequest request, CredentialContext credential) {
        try {
            String eventId = request.getStringParam("eventId");

            if (eventId == null || eventId.isBlank()) {
                return ExecuteResponse.error(400, "eventId is required");
            }

            String path = "/me/events/" + eventId;
            GraphApiResponse response = graphApiClient.delete(path, credential.accessToken());

            if (response.isSuccessful()) {
                log.info("Successfully deleted event: {}", eventId);
                return ExecuteResponse.success(response.statusCode(), "{\"success\": true, \"eventId\": \"" + eventId + "\"}");
            } else {
                log.warn("Failed to delete event: {}", response.body());
                return ExecuteResponse.error(response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to delete event", e);
            return ExecuteResponse.error("Failed to delete event: " + e.getMessage());
        }
    }
}
