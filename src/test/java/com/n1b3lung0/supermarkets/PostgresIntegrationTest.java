package com.n1b3lung0.supermarkets;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for all Spring Boot integration tests requiring a real PostgreSQL database. The
 * container is a JVM-level singleton — starts once, shared across all
 * subclasses. @TestPropertySource ensures all subclasses share the same Spring context key so the
 * context (and Flyway migrations) runs exactly once.
 */
@TestPropertySource(
    properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=none"})
public abstract class PostgresIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES;

  static {
    POSTGRES =
        new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("supermarkets_test")
            .withUsername("test")
            .withPassword("test");
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void configureDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }
}
