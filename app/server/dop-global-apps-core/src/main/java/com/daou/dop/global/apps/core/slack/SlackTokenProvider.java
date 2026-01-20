package com.daou.dop.global.apps.core.slack;

import com.daou.dop.global.apps.core.slack.dto.SlackInstallation;

import java.util.Optional;

/**
 * Slack 워크스페이스 토큰 조회 인터페이스
 */
public interface SlackTokenProvider {

    /**
     * Team ID로 설치 정보 조회
     *
     * @param teamId Slack Team ID
     * @return 설치 정보 (없으면 empty)
     */
    Optional<SlackInstallation> findByTeamId(String teamId);

    /**
     * 설치 정보 저장/갱신
     *
     * @param installation 설치 정보
     */
    void save(SlackInstallation installation);
}
