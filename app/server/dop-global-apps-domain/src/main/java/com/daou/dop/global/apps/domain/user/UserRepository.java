package com.daou.dop.global.apps.domain.user;

import com.daou.dop.global.apps.domain.enums.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * User Repository 인터페이스
 *
 * <p>infrastructure 모듈에서 JPA 구현
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByCompanyIdAndPlatformUserId(Long companyId, String platformUserId);

    List<User> findByCompanyId(Long companyId);

    List<User> findByCompanyIdAndStatus(Long companyId, UserStatus status);

    Optional<User> findByEmail(String email);

    boolean existsByCompanyIdAndPlatformUserId(Long companyId, String platformUserId);

    User save(User user);

    void deleteById(Long id);
}
