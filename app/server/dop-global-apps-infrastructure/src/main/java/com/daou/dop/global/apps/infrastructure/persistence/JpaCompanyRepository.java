package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.domain.company.Company;
import com.daou.dop.global.apps.domain.company.CompanyRepository;
import com.daou.dop.global.apps.domain.enums.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Company JPA Repository
 */
@Repository
public interface JpaCompanyRepository extends JpaRepository<Company, Long>, CompanyRepository {

    @Override
    Optional<Company> findByCompanyUid(String companyUid);

    @Override
    List<Company> findByStatus(CompanyStatus status);

    @Override
    boolean existsByCompanyUid(String companyUid);
}
