package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.domain.enums.UserStatus;
import com.daou.dop.global.apps.domain.user.User;
import com.daou.dop.global.apps.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User JPA Repository
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

    @Override
    Optional<User> findByCompanyIdAndPlatformUserId(Long companyId, String platformUserId);

    @Override
    List<User> findByCompanyId(Long companyId);

    @Override
    List<User> findByCompanyIdAndStatus(Long companyId, UserStatus status);

    @Override
    Optional<User> findByEmail(String email);

    @Override
    boolean existsByCompanyIdAndPlatformUserId(Long companyId, String platformUserId);
}
