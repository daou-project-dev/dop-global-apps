package com.daou.dop.global.apps.domain.credential;

import java.util.Optional;

/**
 * OAuthCredential Repository 인터페이스
 *
 * <p>infrastructure 모듈에서 JPA 구현
 */
public interface OAuthCredentialRepository {

    Optional<OAuthCredential> findById(Long id);

    Optional<OAuthCredential> findByConnectionId(Long connectionId);

    boolean existsByConnectionId(Long connectionId);

    OAuthCredential save(OAuthCredential credential);

    void deleteByConnectionId(Long connectionId);
}
