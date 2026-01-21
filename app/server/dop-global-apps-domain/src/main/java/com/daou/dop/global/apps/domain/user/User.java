package com.daou.dop.global.apps.domain.user;

import com.daou.dop.global.apps.domain.enums.UserStatus;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * 사용자 Entity
 *
 * <p>외부 서비스에서 프로비저닝된 사용자 정보 관리
 * <p>company_id는 논리적 참조 (FK 제약 없음)
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_company_platform", columnList = "companyId, platformUserId", unique = true),
        @Index(name = "idx_user_company_id", columnList = "companyId")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 고객사 ID (논리적 참조)
     */
    @Column(nullable = false)
    private Long companyId;

    /**
     * 플랫폼 사용자 ID (프로비저닝)
     */
    @Column(nullable = false, length = 100)
    private String platformUserId;

    /**
     * 로그인 ID
     */
    @Column(length = 100)
    private String loginId;

    /**
     * 사용자명
     */
    @Column(length = 100)
    private String name;

    /**
     * 이메일
     */
    @Column(length = 200)
    private String email;

    /**
     * 사용자 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    protected User() {
    }

    private User(Builder builder) {
        this.companyId = builder.companyId;
        this.platformUserId = builder.platformUserId;
        this.loginId = builder.loginId;
        this.name = builder.name;
        this.email = builder.email;
        this.status = builder.status != null ? builder.status : UserStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getPlatformUserId() { return platformUserId; }
    public String getLoginId() { return loginId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    // Update methods
    public void updateInfo(String loginId, String name, String email) {
        this.loginId = loginId;
        this.name = name;
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private Long companyId;
        private String platformUserId;
        private String loginId;
        private String name;
        private String email;
        private UserStatus status;

        public Builder companyId(Long companyId) { this.companyId = companyId; return this; }
        public Builder platformUserId(String platformUserId) { this.platformUserId = platformUserId; return this; }
        public Builder loginId(String loginId) { this.loginId = loginId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder status(UserStatus status) { this.status = status; return this; }

        public User build() {
            return new User(this);
        }
    }
}
