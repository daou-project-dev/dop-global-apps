package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.core.repository.OAuthCredentialRepository;
import com.daou.dop.global.apps.domain.credential.OAuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OAuthCredential JPA Repository
 */
@Repository
public interface JpaOAuthCredentialRepository extends JpaRepository<OAuthCredential, Long>, OAuthCredentialRepository {

    Optional<OAuthCredential> findByConnectionId(Long connectionId);

    boolean existsByConnectionId(Long connectionId);

    void deleteByConnectionId(Long connectionId);
}
