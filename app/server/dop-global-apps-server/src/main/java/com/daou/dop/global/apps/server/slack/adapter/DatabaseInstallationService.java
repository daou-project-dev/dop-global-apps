package com.daou.dop.global.apps.server.slack.adapter;

import com.daou.dop.global.apps.core.slack.SlackTokenProvider;
import com.daou.dop.global.apps.core.slack.dto.SlackInstallation;
import com.slack.api.bolt.model.Bot;
import com.slack.api.bolt.model.Installer;
import com.slack.api.bolt.model.builtin.DefaultBot;
import com.slack.api.bolt.model.builtin.DefaultInstaller;
import com.slack.api.bolt.service.InstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DB 기반 Slack Installation 관리
 */
public class DatabaseInstallationService implements InstallationService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInstallationService.class);

    private final SlackTokenProvider tokenProvider;

    public DatabaseInstallationService(SlackTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean isHistoricalDataEnabled() {
        return false;
    }

    @Override
    public void setHistoricalDataEnabled(boolean isHistoricalDataEnabled) {
        // Not supported
    }

    @Override
    public void saveInstallerAndBot(Installer installer) throws Exception {
        SlackInstallation installation = SlackInstallation.builder()
                .teamId(installer.getTeamId())
                .teamName(installer.getTeamId())  // TeamName은 OAuth 콜백에서 별도 처리
                .accessToken(installer.getBotAccessToken())
                .botUserId(installer.getBotUserId())
                .scope(installer.getBotScope())
                .installedAt(java.time.Instant.now())
                .build();

        tokenProvider.save(installation);
        log.info("Saved Slack installation for team: {}", installer.getTeamId());
    }

    @Override
    public void deleteBot(Bot bot) throws Exception {
        log.info("Delete bot requested for team: {}", bot.getTeamId());
    }

    @Override
    public void deleteInstaller(Installer installer) throws Exception {
        log.info("Delete installer requested for team: {}", installer.getTeamId());
    }

    @Override
    public Bot findBot(String enterpriseId, String teamId) {
        return tokenProvider.findByTeamId(teamId)
                .map(installation -> {
                    DefaultBot bot = new DefaultBot();
                    bot.setTeamId(teamId);
                    bot.setEnterpriseId(enterpriseId);
                    bot.setBotAccessToken(installation.accessToken());
                    bot.setBotUserId(installation.botUserId());
                    bot.setScope(installation.scope());
                    return (Bot) bot;
                })
                .orElse(null);
    }

    @Override
    public Installer findInstaller(String enterpriseId, String teamId, String installerUserId) {
        return tokenProvider.findByTeamId(teamId)
                .map(installation -> {
                    DefaultInstaller installer = new DefaultInstaller();
                    installer.setTeamId(teamId);
                    installer.setTeamName(installation.teamName());
                    installer.setEnterpriseId(enterpriseId);
                    installer.setBotAccessToken(installation.accessToken());
                    installer.setBotUserId(installation.botUserId());
                    installer.setBotScope(installation.scope());
                    return (Installer) installer;
                })
                .orElse(null);
    }

    @Override
    public void deleteAll(String enterpriseId, String teamId) {
        log.info("Delete all requested for team: {}", teamId);
    }
}
