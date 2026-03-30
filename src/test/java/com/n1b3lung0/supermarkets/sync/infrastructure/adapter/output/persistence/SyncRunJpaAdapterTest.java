package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRun;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncStatus;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.mapper.SyncRunPersistenceMapper;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.repository.SpringSyncRunRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Step 59 — integration tests for SyncRunJpaAdapter using Testcontainers PostgreSQL. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SyncRunJpaAdapterTest extends PostgresIntegrationTest {

  @Autowired private SpringSyncRunRepository springRepository;

  private SyncRunJpaAdapter adapter() {
    return new SyncRunJpaAdapter(springRepository, new SyncRunPersistenceMapper());
  }

  @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  private UUID createSupermarket() {
    var id = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO supermarkets (id, name, country, created_at, updated_at) "
            + "VALUES (?, ?, ?, NOW(), NOW())",
        id,
        "Test Supermarket " + id,
        "ES");
    return id;
  }

  @Test
  void save_andFindById_shouldRoundTrip() {
    var supermarketId = SupermarketId.of(createSupermarket());
    var adapter = adapter();
    var run = SyncRun.start(supermarketId);

    adapter.save(run);
    var found = adapter.findById(run.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(run.getId());
    assertThat(found.get().getStatus()).isEqualTo(SyncStatus.RUNNING);
    assertThat(found.get().getSupermarketId()).isEqualTo(supermarketId);
  }

  @Test
  void save_completed_shouldPersistCounters() {
    var supermarketId = SupermarketId.of(createSupermarket());
    var adapter = adapter();
    var run = SyncRun.start(supermarketId);
    run.complete(50, 2000, 5);

    adapter.save(run);
    var found = adapter.findById(run.getId()).orElseThrow();

    assertThat(found.getStatus()).isEqualTo(SyncStatus.COMPLETED);
    assertThat(found.getCategoriesSynced()).isEqualTo(50);
    assertThat(found.getProductsSynced()).isEqualTo(2000);
    assertThat(found.getProductsDeactivated()).isEqualTo(5);
    assertThat(found.getFinishedAt()).isNotNull();
  }

  @Test
  void save_failed_shouldPersistErrorMessage() {
    var supermarketId = SupermarketId.of(createSupermarket());
    var adapter = adapter();
    var run = SyncRun.start(supermarketId);
    run.fail("Timeout connecting to API");

    adapter.save(run);
    var found = adapter.findById(run.getId()).orElseThrow();

    assertThat(found.getStatus()).isEqualTo(SyncStatus.FAILED);
    assertThat(found.getErrorMessage()).isEqualTo("Timeout connecting to API");
  }

  @Test
  void findBySupermarketId_shouldReturnPagedResults() {
    var supermarketId = SupermarketId.of(createSupermarket());
    var adapter = adapter();

    var run1 = SyncRun.start(supermarketId);
    run1.complete(10, 100, 0);
    adapter.save(run1);

    var run2 = SyncRun.start(supermarketId);
    run2.fail("error");
    adapter.save(run2);

    var page = adapter.findBySupermarketId(supermarketId.value(), PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent())
        .extracting(v -> v.status())
        .containsExactlyInAnyOrder("COMPLETED", "FAILED");
  }

  @Test
  void findById_nonExistent_shouldReturnEmpty() {
    var adapter = adapter();
    var found = adapter.findById(SyncRunId.generate());
    assertThat(found).isEmpty();
  }
}
