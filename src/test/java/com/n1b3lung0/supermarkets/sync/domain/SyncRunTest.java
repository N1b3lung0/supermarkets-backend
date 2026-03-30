package com.n1b3lung0.supermarkets.sync.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRun;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Step 57 — unit tests for SyncRun domain aggregate state transitions. */
class SyncRunTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  @Test
  void start_shouldCreateRunningRun() {
    var run = SyncRun.start(SUPERMARKET);

    assertThat(run.getId()).isNotNull();
    assertThat(run.getSupermarketId()).isEqualTo(SUPERMARKET);
    assertThat(run.getStatus()).isEqualTo(SyncStatus.RUNNING);
    assertThat(run.getStartedAt()).isNotNull();
    assertThat(run.getFinishedAt()).isNull();
    assertThat(run.getErrorMessage()).isNull();
    assertThat(run.getCategoriesSynced()).isZero();
    assertThat(run.getProductsSynced()).isZero();
    assertThat(run.getProductsDeactivated()).isZero();
  }

  @Test
  void complete_shouldTransitionToCompleted() {
    var run = SyncRun.start(SUPERMARKET);

    run.complete(100, 5000, 12);

    assertThat(run.getStatus()).isEqualTo(SyncStatus.COMPLETED);
    assertThat(run.getFinishedAt()).isNotNull();
    assertThat(run.getCategoriesSynced()).isEqualTo(100);
    assertThat(run.getProductsSynced()).isEqualTo(5000);
    assertThat(run.getProductsDeactivated()).isEqualTo(12);
    assertThat(run.getErrorMessage()).isNull();
  }

  @Test
  void fail_shouldTransitionToFailed() {
    var run = SyncRun.start(SUPERMARKET);

    run.fail("Network timeout");

    assertThat(run.getStatus()).isEqualTo(SyncStatus.FAILED);
    assertThat(run.getFinishedAt()).isNotNull();
    assertThat(run.getErrorMessage()).isEqualTo("Network timeout");
  }

  @Test
  void complete_onNonRunning_shouldThrow() {
    var run = SyncRun.start(SUPERMARKET);
    run.complete(1, 1, 0);

    assertThatThrownBy(() -> run.complete(2, 2, 0))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("RUNNING");
  }

  @Test
  void fail_onNonRunning_shouldThrow() {
    var run = SyncRun.start(SUPERMARKET);
    run.fail("first failure");

    assertThatThrownBy(() -> run.fail("second failure"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("RUNNING");
  }

  @Test
  void start_withNullSupermarketId_shouldThrow() {
    assertThatThrownBy(() -> SyncRun.start(null)).isInstanceOf(NullPointerException.class);
  }
}
