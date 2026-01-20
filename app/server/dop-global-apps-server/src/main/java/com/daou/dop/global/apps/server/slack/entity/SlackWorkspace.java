package com.daou.dop.global.apps.server.slack.entity;

import com.daou.dop.global.apps.core.crypto.EncryptedStringConverter;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Slack 워크스페이스 설치 정보 Entity
 */
@Entity
@Table(name = "slack_workspace", indexes = {
        @Index(name = "idx_slack_workspace_team_id", columnList = "teamId", unique = true)
})
public class SlackWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String teamId;

    @Column(nullable = false)
    private String teamName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String accessToken;

    @Column(nullable = false)
    private String botUserId;

    @Column(length = 1000)
    private String scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE;

    @Column(nullable = false)
    private Instant installedAt;

    @Column
    private Instant updatedAt;

    protected SlackWorkspace() {
    }

    private SlackWorkspace(Builder builder) {
        this.teamId = builder.teamId;
        this.teamName = builder.teamName;
        this.accessToken = builder.accessToken;
        this.botUserId = builder.botUserId;
        this.scope = builder.scope;
        this.status = builder.status != null ? builder.status : WorkspaceStatus.ACTIVE;
        this.installedAt = builder.installedAt != null ? builder.installedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getBotUserId() {
        return botUserId;
    }

    public String getScope() {
        return scope;
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public Instant getInstalledAt() {
        return installedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Update methods
    public void updateToken(String accessToken, String scope) {
        this.accessToken = accessToken;
        this.scope = scope;
        this.updatedAt = Instant.now();
    }

    public void updateStatus(WorkspaceStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private String teamId;
        private String teamName;
        private String accessToken;
        private String botUserId;
        private String scope;
        private WorkspaceStatus status;
        private Instant installedAt;

        public Builder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public Builder teamName(String teamName) {
            this.teamName = teamName;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder botUserId(String botUserId) {
            this.botUserId = botUserId;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder status(WorkspaceStatus status) {
            this.status = status;
            return this;
        }

        public Builder installedAt(Instant installedAt) {
            this.installedAt = installedAt;
            return this;
        }

        public SlackWorkspace build() {
            return new SlackWorkspace(this);
        }
    }
}
