package com.daou.dop.gapps.infrastructure.persistence;

import com.daou.dop.gapps.domain.enums.UserStatus;
import com.daou.dop.gapps.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User JPA Repository
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCompanyIdAndPlatformUserId(Long companyId, String platformUserId);

    List<User> findByCompanyId(Long companyId);

    List<User> findByCompanyIdAndStatus(Long companyId, UserStatus status);

    Optional<User> findByEmail(String email);

    boolean existsByCompanyIdAndPlatformUserId(Long companyId, String platformUserId);
}
