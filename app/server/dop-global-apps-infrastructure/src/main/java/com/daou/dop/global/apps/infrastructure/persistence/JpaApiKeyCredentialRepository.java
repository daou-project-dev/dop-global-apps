package com.daou.dop.global.apps.infrastructure.persistence;

import com.daou.dop.global.apps.domain.credential.ApiKeyCredential;
import com.daou.dop.global.apps.domain.credential.ApiKeyCredentialRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ApiKeyCredential JPA Repository
 */
@Repository
public interface JpaApiKeyCredentialRepository extends JpaRepository<ApiKeyCredential, Long>, ApiKeyCredentialRepository {

    @Override
    Optional<ApiKeyCredential> findByConnectionId(Long connectionId);

    @Override
    boolean existsByConnectionId(Long connectionId);

    @Override
    void deleteByConnectionId(Long connectionId);
}
