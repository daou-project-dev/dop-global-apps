package com.daou.dop.global.apps.server.slack.controller;

import com.daou.dop.global.apps.server.slack.adapter.SlackBoltAdapter;
import com.daou.dop.global.apps.server.slack.service.SlackOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Slack 이벤트/커맨드/인터랙션/OAuth 엔드포인트
 */
@RestController
@RequestMapping("/slack")
public class SlackController {

    private final SlackBoltAdapter boltAdapter;
    private final SlackOAuthService oAuthService;

    public SlackController(SlackBoltAdapter boltAdapter, SlackOAuthService oAuthService) {
        this.boltAdapter = boltAdapter;
        this.oAuthService = oAuthService;
    }

    /**
     * Slack 이벤트 수신 (Event Subscriptions)
     */
    @PostMapping("/events")
    public void handleEvents(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boltAdapter.handleServletRequest(request, response);
    }

    /**
     * 슬래시 커맨드 처리
     */
    @PostMapping("/commands")
    public void handleCommands(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boltAdapter.handleServletRequest(request, response);
    }

    /**
     * 인터랙션 처리 (버튼, 모달 등)
     */
    @PostMapping("/interactions")
    public void handleInteractions(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boltAdapter.handleServletRequest(request, response);
    }

    /**
     * OAuth 설치 시작
     */
    @GetMapping("/install")
    public void startInstall(HttpServletResponse response) throws Exception {
        String installUrl = oAuthService.getInstallUrl();
        response.sendRedirect(installUrl);
    }

    /**
     * OAuth 콜백
     */
    @GetMapping("/oauth/callback")
    public String handleOAuthCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error) {

        if (error != null) {
            return "Installation failed: " + error;
        }

        try {
            oAuthService.handleCallback(code, state);
            return "Installation successful! You can close this window.";
        } catch (Exception e) {
            return "Installation failed: " + e.getMessage();
        }
    }
}
