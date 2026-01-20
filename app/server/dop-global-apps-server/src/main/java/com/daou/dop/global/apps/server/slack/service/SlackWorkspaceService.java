package com.daou.dop.global.apps.server.slack.service;

import com.daou.dop.global.apps.server.slack.entity.SlackWorkspace;
import com.daou.dop.global.apps.server.slack.entity.WorkspaceStatus;
import com.daou.dop.global.apps.server.slack.repository.SlackWorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SlackWorkspaceService {

    private final SlackWorkspaceRepository workspaceRepository;

    public SlackWorkspaceService(SlackWorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public Optional<SlackWorkspace> findByTeamId(String teamId) {
        return workspaceRepository.findByTeamId(teamId);
    }

    public List<SlackWorkspace> findActiveWorkspaces() {
        return workspaceRepository.findByStatus(WorkspaceStatus.ACTIVE);
    }

    @Transactional
    public SlackWorkspace saveOrUpdate(SlackWorkspace workspace) {
        return workspaceRepository.findByTeamId(workspace.getTeamId())
                .map(existing -> {
                    existing.updateToken(workspace.getAccessToken(), workspace.getScope());
                    existing.updateStatus(WorkspaceStatus.ACTIVE);
                    return workspaceRepository.save(existing);
                })
                .orElseGet(() -> workspaceRepository.save(workspace));
    }

    @Transactional
    public void revokeWorkspace(String teamId) {
        workspaceRepository.findByTeamId(teamId)
                .ifPresent(workspace -> workspace.updateStatus(WorkspaceStatus.REVOKED));
    }
}
