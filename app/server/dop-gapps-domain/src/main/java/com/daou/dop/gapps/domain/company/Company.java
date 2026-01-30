package com.daou.dop.gapps.domain.company;

import com.daou.dop.gapps.domain.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 고객사 Entity
 *
 * <p>외부 서비스에서 프로비저닝된 고객사 정보 관리
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "company", indexes = {
        @Index(name = "idx_company_company_uid", columnList = "companyUid", unique = true)
})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 고객사 고유 식별자 (프로비저닝)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String companyUid;

    /**
     * 고객사명
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 고객사 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public boolean isOnline() {
        return status == CompanyStatus.ONLINE;
    }

    // Update methods
    public void updateName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setOnline() {
        this.status = CompanyStatus.ONLINE;
        this.updatedAt = Instant.now();
    }

    public void setOffline() {
        this.status = CompanyStatus.OFFLINE;
        this.updatedAt = Instant.now();
    }
}
