package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.domain.credential.ApiKeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ApiKeyCredential JPA Repository
 */
@Repository
public interface JpaApiKeyCredentialRepository extends JpaRepository<ApiKeyCredential, Long> {

    Optional<ApiKeyCredential> findByConnectionId(Long connectionId);

    boolean existsByConnectionId(Long connectionId);

    void deleteByConnectionId(Long connectionId);
}
