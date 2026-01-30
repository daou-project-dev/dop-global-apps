package com.daou.dop.gapps.plugin.google.calendar;

import com.daou.dop.gapps.plugin.google.calendar.handler.*;
import com.daou.dop.gapps.plugin.google.calendar.service.GoogleAuthService;
import com.daou.dop.gapps.plugin.google.calendar.service.GoogleCalendarService;
import com.daou.dop.gapps.plugin.sdk.CredentialContext;
import com.daou.dop.gapps.plugin.sdk.ExecuteRequest;
import com.daou.dop.gapps.plugin.sdk.ExecuteResponse;
import com.daou.dop.gapps.plugin.sdk.PluginExecutor;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.calendar.Calendar;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Calendar API 실행을 위한 PluginExecutor 구현
 * Handler 패턴 적용
 */
@Extension
public class GoogleCalendarPluginExecutor implements PluginExecutor {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarPluginExecutor.class);
    private static final String PLUGIN_ID = "google-calendar";

    private final GoogleAuthService authService = new GoogleAuthService();
    private final Map<String, ActionHandler> handlers = new HashMap<>();

    public GoogleCalendarPluginExecutor() {
        registerHandlers();
    }

    private void registerHandlers() {
        registerHandler(new CalendarListHandler());
        registerHandler(new EventsListHandler());
        registerHandler(new EventsGetHandler());
        registerHandler(new EventsCreateHandler());
        registerHandler(new EventsUpdateHandler());
        registerHandler(new EventsDeleteHandler());

        log.info("Registered {} action handlers for {}", handlers.size(), PLUGIN_ID);
    }

    private void registerHandler(ActionHandler handler) {
        handlers.put(handler.getAction(), handler);
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public List<String> getSupportedActions() {
        return List.copyOf(handlers.keySet());
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        // TODO: 운영 시 인증 검증 복원 필요
        // CredentialContext credential = request.credential();
        // if (credential == null || !credential.isApiKey()) {
        //     return ExecuteResponse.error(401, "API key (JSON key path) required");
        // }

        // 로컬 테스트: credential 무시하고 하드코딩된 JSON 키 사용
        CredentialContext credential = request.credential();

        String action = request.action();
        if (action == null || action.isBlank()) {
            return ExecuteResponse.error(400, "action is required");
        }

        ActionHandler handler = handlers.get(action);
        if (handler == null) {
            return ExecuteResponse.error(400, "Unsupported action: " + action);
        }

        log.debug("Executing action: {} with handler: {}", action, handler.getClass().getSimpleName());

        try {
            // Calendar 서비스 생성
            Calendar calendarApi = authService.createCalendarService(credential);
            GoogleCalendarService calendarService = new GoogleCalendarService(calendarApi);

            // Handler에 위임
            return handler.handle(request, calendarService);

        } catch (GoogleJsonResponseException e) {
            return handleGoogleApiError(e);
        } catch (IOException e) {
            log.error("IO error", e);
            return ExecuteResponse.error(500, "IO error: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            log.error("Security error", e);
            return ExecuteResponse.error(500, "Security error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ExecuteResponse.error(500, "Unexpected error: " + e.getMessage());
        }
    }

    private ExecuteResponse handleGoogleApiError(GoogleJsonResponseException e) {
        int statusCode = e.getStatusCode();
        String message = e.getDetails() != null ? e.getDetails().getMessage() : e.getMessage();

        log.warn("Google API error: {} - {}", statusCode, message);

        return switch (statusCode) {
            case 401 -> ExecuteResponse.error(401, "Authentication failed: " + message);
            case 403 -> ExecuteResponse.error(403, "Permission denied: " + message);
            case 404 -> ExecuteResponse.error(404, "Resource not found: " + message);
            case 429 -> ExecuteResponse.error(429, "Rate limit exceeded: " + message);
            default -> ExecuteResponse.error(statusCode, "Google API error: " + message);
        };
    }
}
