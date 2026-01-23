package com.daou.dop.global.apps.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 * - @EntityScan은 Application 클래스에서 설정 (auto-config 우선순위)
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.daou.dop.global.apps.infrastructure.persistence")
public class JpaConfig {
}
