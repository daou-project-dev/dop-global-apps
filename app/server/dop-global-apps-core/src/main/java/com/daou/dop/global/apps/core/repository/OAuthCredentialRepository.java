package com.daou.dop.global.apps.core.repository;

import com.daou.dop.global.apps.domain.credential.OAuthCredential;

import java.util.Optional;

/**
 * OAuthCredential Repository Port
 */
public interface OAuthCredentialRepository {

    Optional<OAuthCredential> findByConnectionId(Long connectionId);

    OAuthCredential save(OAuthCredential credential);

    void deleteByConnectionId(Long connectionId);
}
