package com.daou.dop.global.apps.domain.credential;

import java.util.Optional;

/**
 * ApiKeyCredential Repository 인터페이스
 *
 * <p>infrastructure 모듈에서 JPA 구현
 */
public interface ApiKeyCredentialRepository {

    Optional<ApiKeyCredential> findById(Long id);

    Optional<ApiKeyCredential> findByConnectionId(Long connectionId);

    boolean existsByConnectionId(Long connectionId);

    ApiKeyCredential save(ApiKeyCredential credential);

    void deleteByConnectionId(Long connectionId);
}
