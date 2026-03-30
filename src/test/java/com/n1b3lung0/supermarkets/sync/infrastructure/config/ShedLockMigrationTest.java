package com.n1b3lung0.supermarkets.sync.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Step 61 — Verifies the ShedLock Flyway migration (V8) ran correctly. The scheduler itself is
 * disabled in the test profile via {@code app.scheduler.sync.enabled=false}.
 */
@SpringBootTest
@ActiveProfiles("test")
class ShedLockMigrationTest extends PostgresIntegrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void shedlockTable_shouldExistAfterMigration() {
    var count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema = 'public' AND table_name = 'shedlock'
            """,
            Integer.class);
    assertThat(count).isEqualTo(1);
  }

  @Test
  void shedlockTable_shouldHaveExpectedColumns() {
    var columns =
        jdbcTemplate.queryForList(
            """
            SELECT column_name FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'shedlock'
            ORDER BY column_name
            """,
            String.class);
    assertThat(columns).containsExactlyInAnyOrder("lock_until", "locked_at", "locked_by", "name");
  }
}
