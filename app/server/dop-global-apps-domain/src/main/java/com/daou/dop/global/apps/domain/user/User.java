package com.daou.dop.global.apps.domain.user;

import com.daou.dop.global.apps.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 사용자 Entity
 *
 * <p>외부 서비스에서 프로비저닝된 사용자 정보 관리
 * <p>company_id는 논리적 참조 (FK 제약 없음)
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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
}
