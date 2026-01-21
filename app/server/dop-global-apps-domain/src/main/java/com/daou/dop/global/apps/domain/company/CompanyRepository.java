package com.daou.dop.global.apps.domain.company;

import com.daou.dop.global.apps.domain.enums.CompanyStatus;

import java.util.List;
import java.util.Optional;

/**
 * Company Repository 인터페이스
 *
 * <p>infrastructure 모듈에서 JPA 구현
 */
public interface CompanyRepository {

    Optional<Company> findById(Long id);

    Optional<Company> findByCompanyUid(String companyUid);

    List<Company> findByStatus(CompanyStatus status);

    List<Company> findAll();

    boolean existsByCompanyUid(String companyUid);

    Company save(Company company);

    void deleteById(Long id);
}
