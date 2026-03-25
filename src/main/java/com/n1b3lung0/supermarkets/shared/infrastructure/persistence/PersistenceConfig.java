package com.n1b3lung0.supermarkets.shared.infrastructure.persistence;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables JPA auditing (createdAt / updatedAt) for all entities with @EntityListeners. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {

  /**
   * Until security is wired (Phase 11), the auditor is a fixed system user. Will be replaced with a
   * SecurityContextHolder-based resolver in the security phase.
   */
  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> Optional.of("system");
  }
}
