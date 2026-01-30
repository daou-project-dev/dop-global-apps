package com.daou.dop.gapps.infrastructure.persistence;

import com.daou.dop.gapps.domain.company.Company;
import com.daou.dop.gapps.domain.enums.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Company JPA Repository
 */
@Repository
public interface JpaCompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCompanyUid(String companyUid);

    List<Company> findByStatus(CompanyStatus status);

    boolean existsByCompanyUid(String companyUid);
}
