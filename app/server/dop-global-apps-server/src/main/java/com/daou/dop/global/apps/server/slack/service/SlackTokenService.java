package com.daou.dop.global.apps.server.slack.service;

import com.daou.dop.global.apps.core.slack.SlackTokenProvider;
import com.daou.dop.global.apps.core.slack.dto.SlackInstallation;
import com.daou.dop.global.apps.server.slack.entity.SlackWorkspace;
import com.daou.dop.global.apps.server.slack.entity.WorkspaceStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SlackTokenProvider 구현체
 * Core 모듈에서 토큰 조회 시 사용
 */
@Service
public class SlackTokenService implements SlackTokenProvider {

    private final SlackWorkspaceService workspaceService;

    public SlackTokenService(SlackWorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    public Optional<SlackInstallation> findByTeamId(String teamId) {
        return workspaceService.findByTeamId(teamId)
                .filter(ws -> ws.getStatus() == WorkspaceStatus.ACTIVE)
                .map(this::toInstallation);
    }

    @Override
    public void save(SlackInstallation installation) {
        SlackWorkspace workspace = SlackWorkspace.builder()
                .teamId(installation.teamId())
                .teamName(installation.teamName())
                .accessToken(installation.accessToken())
                .botUserId(installation.botUserId())
                .scope(installation.scope())
                .installedAt(installation.installedAt())
                .build();

        workspaceService.saveOrUpdate(workspace);
    }

    private SlackInstallation toInstallation(SlackWorkspace workspace) {
        return SlackInstallation.builder()
                .teamId(workspace.getTeamId())
                .teamName(workspace.getTeamName())
                .accessToken(workspace.getAccessToken())
                .botUserId(workspace.getBotUserId())
                .scope(workspace.getScope())
                .installedAt(workspace.getInstalledAt())
                .build();
    }
}
