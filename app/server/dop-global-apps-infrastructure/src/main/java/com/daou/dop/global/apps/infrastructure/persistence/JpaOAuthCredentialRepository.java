package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.domain.credential.OAuthCredential;
import com.daou.dop.global.apps.domain.credential.OAuthCredentialRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OAuthCredential JPA Repository
 */
@Repository
public interface JpaOAuthCredentialRepository extends JpaRepository<OAuthCredential, Long>, OAuthCredentialRepository {

    @Override
    Optional<OAuthCredential> findByConnectionId(Long connectionId);

    @Override
    boolean existsByConnectionId(Long connectionId);

    @Override
    void deleteByConnectionId(Long connectionId);
}
