package com.daou.dop.gapps.api.oauth.controller;

import com.daou.dop.gapps.api.oauth.facade.OAuthInstallException;
import com.daou.dop.gapps.api.oauth.facade.OAuthInstallFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 범용 OAuth 컨트롤러
 * HTTP 요청/응답 처리만 담당, 비즈니스 로직은 Facade에 위임
 */
@RestController
@RequestMapping("/oauth")
public class PluginOAuthController {

    private static final Logger log = LoggerFactory.getLogger(PluginOAuthController.class);

    private final OAuthInstallFacade oAuthInstallFacade;

    public PluginOAuthController(OAuthInstallFacade oAuthInstallFacade) {
        this.oAuthInstallFacade = oAuthInstallFacade;
    }

    /**
     * OAuth 설치 시작
     * GET /oauth/{plugin}/install
     */
    @GetMapping("/{plugin}/install")
    public ResponseEntity<String> startInstall(
            @PathVariable("plugin") String pluginId,
            HttpServletRequest request) {

        try {
            String redirectUri = buildRedirectUri(request, pluginId);
            String authorizationUrl = oAuthInstallFacade.startInstall(pluginId, redirectUri);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", authorizationUrl)
                    .build();
        } catch (OAuthInstallException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    /**
     * OAuth 콜백
     * GET /oauth/{plugin}/callback
     */
    @GetMapping("/{plugin}/callback")
    public ResponseEntity<String> handleCallback(
            @PathVariable("plugin") String pluginId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request) {

        if (error != null) {
            log.warn("OAuth error for plugin {}: {}", pluginId, error);
            return ResponseEntity.badRequest().body("Installation failed: " + error);
        }

        try {
            String redirectUri = buildRedirectUri(request, pluginId);
            oAuthInstallFacade.handleCallback(pluginId, code, state, redirectUri);

            return ResponseEntity.ok("Installation successful! You can close this window.");
        } catch (OAuthInstallException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    private String buildRedirectUri(HttpServletRequest request, String pluginId) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder uri = new StringBuilder();
        uri.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && serverPort != 80) ||
                ("https".equals(scheme) && serverPort != 443)) {
            uri.append(":").append(serverPort);
        }

        uri.append(contextPath);
        uri.append("/oauth/").append(pluginId).append("/callback");

        return uri.toString();
    }
}
