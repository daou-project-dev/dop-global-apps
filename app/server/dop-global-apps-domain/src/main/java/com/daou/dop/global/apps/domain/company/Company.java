package com.daou.dop.global.apps.domain.company;

import com.daou.dop.global.apps.domain.enums.CompanyStatus;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * 고객사 Entity
 *
 * <p>외부 서비스에서 프로비저닝된 고객사 정보 관리
 */
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

    protected Company() {
    }

    private Company(Builder builder) {
        this.companyUid = builder.companyUid;
        this.name = builder.name;
        this.status = builder.status != null ? builder.status : CompanyStatus.ONLINE;
        this.createdAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Long getId() { return id; }
    public String getCompanyUid() { return companyUid; }
    public String getName() { return name; }
    public CompanyStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

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

    public static class Builder {
        private String companyUid;
        private String name;
        private CompanyStatus status;

        public Builder companyUid(String companyUid) { this.companyUid = companyUid; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder status(CompanyStatus status) { this.status = status; return this; }

        public Company build() {
            return new Company(this);
        }
    }
}
