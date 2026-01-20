package com.daou.dop.global.apps.server.slack.repository;

import com.daou.dop.global.apps.server.slack.entity.SlackWorkspace;
import com.daou.dop.global.apps.server.slack.entity.WorkspaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlackWorkspaceRepository extends JpaRepository<SlackWorkspace, Long> {

    Optional<SlackWorkspace> findByTeamId(String teamId);

    List<SlackWorkspace> findByStatus(WorkspaceStatus status);

    boolean existsByTeamId(String teamId);
}
