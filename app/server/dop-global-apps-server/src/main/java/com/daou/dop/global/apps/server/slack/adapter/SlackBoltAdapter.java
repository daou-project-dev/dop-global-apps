package com.daou.dop.global.apps.server.slack.adapter;

import com.daou.dop.global.apps.core.slack.SlackBoltExtension;
import com.daou.dop.global.apps.core.slack.SlackTokenProvider;
import com.daou.dop.global.apps.server.slack.SlackProperties;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.util.SlackRequestParser;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Slack Bolt App 관리 및 플러그인 핸들러 등록
 */
@Component
@EnableConfigurationProperties(SlackProperties.class)
public class SlackBoltAdapter {

    private static final Logger log = LoggerFactory.getLogger(SlackBoltAdapter.class);

    private final SlackProperties properties;
    private final PluginManager pluginManager;
    private final SlackTokenProvider tokenProvider;
    private App app;
    private SlackRequestParser requestParser;

    public SlackBoltAdapter(SlackProperties properties,
                            PluginManager pluginManager,
                            SlackTokenProvider tokenProvider) {
        this.properties = properties;
        this.pluginManager = pluginManager;
        this.tokenProvider = tokenProvider;
    }

    @PostConstruct
    public void initialize() {
        AppConfig config = AppConfig.builder()
                .singleTeamBotToken(null)
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .signingSecret(properties.signingSecret())
                .scope(properties.scopes())
                .build();

        this.app = new App(config);
        this.requestParser = new SlackRequestParser(config);

        configureInstallationService();
        registerPluginHandlers();

        log.info("Slack Bolt App initialized");
    }

    private void configureInstallationService() {
        app.service(new DatabaseInstallationService(tokenProvider));
    }

    private void registerPluginHandlers() {
        List<SlackBoltExtension> extensions = pluginManager.getExtensions(SlackBoltExtension.class);

        extensions.stream()
                .sorted(Comparator.comparingInt(SlackBoltExtension::getOrder))
                .forEach(extension -> {
                    log.info("Registering Slack handlers from: {}", extension.getClass().getName());
                    extension.configureHandlers(app);
                });

        log.info("Registered {} Slack Bolt extensions", extensions.size());
    }

    public App getApp() {
        return app;
    }

    /**
     * Jakarta Servlet 요청을 Bolt Request로 변환하여 처리
     */
    public void handleServletRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws Exception {
        String requestBody = getRequestBody(servletRequest);
        RequestHeaders headers = extractHeaders(servletRequest);

        SlackRequestParser.HttpRequest httpRequest = SlackRequestParser.HttpRequest.builder()
                .requestUri(servletRequest.getRequestURI())
                .queryString(servletRequest.getQueryString() != null ? parseQueryString(servletRequest.getQueryString()) : Collections.emptyMap())
                .headers(headers)
                .requestBody(requestBody)
                .remoteAddress(servletRequest.getRemoteAddr())
                .build();

        Request<?> slackRequest = requestParser.parse(httpRequest);
        Response slackResponse = app.run(slackRequest);

        writeResponse(servletResponse, slackResponse);
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private RequestHeaders extractHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            List<String> values = Collections.list(request.getHeaders(name));
            headers.put(name, values);
        }

        return new RequestHeaders(headers);
    }

    private Map<String, List<String>> parseQueryString(String queryString) {
        Map<String, List<String>> params = new HashMap<>();
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=", 2);
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : "";
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return params;
    }

    private void writeResponse(HttpServletResponse servletResponse, Response slackResponse) throws IOException {
        servletResponse.setStatus(slackResponse.getStatusCode());

        if (slackResponse.getContentType() != null) {
            servletResponse.setContentType(slackResponse.getContentType());
        }

        if (slackResponse.getHeaders() != null) {
            slackResponse.getHeaders().forEach((name, values) -> {
                for (String value : values) {
                    servletResponse.addHeader(name, value);
                }
            });
        }

        if (slackResponse.getBody() != null) {
            servletResponse.getWriter().write(slackResponse.getBody());
        }
    }
}
