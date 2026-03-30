package com.n1b3lung0.supermarkets.sync.domain.model;

import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root — records the outcome of one catalog sync execution.
 *
 * <p>Lifecycle:
 *
 * <pre>
 *   SyncRun.start(supermarketId)   → status = RUNNING
 *   .complete(cats, prods, deact)  → status = COMPLETED
 *   .fail(message)                 → status = FAILED
 * </pre>
 */
public class SyncRun {

  private final SyncRunId id;
  private final SupermarketId supermarketId;
  private final Instant startedAt;
  private Instant finishedAt;
  private SyncStatus status;
  private int categoriesSynced;
  private int productsSynced;
  private int productsDeactivated;
  private String errorMessage;

  private SyncRun(SyncRunId id, SupermarketId supermarketId, Instant startedAt) {
    this.id = id;
    this.supermarketId = supermarketId;
    this.startedAt = startedAt;
    this.status = SyncStatus.RUNNING;
  }

  /** Factory — creates a new RUNNING sync run. */
  public static SyncRun start(SupermarketId supermarketId) {
    Objects.requireNonNull(supermarketId, "supermarketId is required");
    return new SyncRun(SyncRunId.generate(), supermarketId, Instant.now());
  }

  /** Reconstitution factory — restores a persisted SyncRun without triggering domain logic. */
  public static SyncRun reconstitute(
      SyncRunId id,
      SupermarketId supermarketId,
      Instant startedAt,
      Instant finishedAt,
      SyncStatus status,
      int categoriesSynced,
      int productsSynced,
      int productsDeactivated,
      String errorMessage) {
    var run = new SyncRun(id, supermarketId, startedAt);
    run.finishedAt = finishedAt;
    run.status = status;
    run.categoriesSynced = categoriesSynced;
    run.productsSynced = productsSynced;
    run.productsDeactivated = productsDeactivated;
    run.errorMessage = errorMessage;
    return run;
  }

  /** Transitions to COMPLETED with counters. */
  public void complete(int categoriesSynced, int productsSynced, int productsDeactivated) {
    if (this.status != SyncStatus.RUNNING) {
      throw new IllegalStateException("SyncRun is not RUNNING — cannot complete");
    }
    this.status = SyncStatus.COMPLETED;
    this.finishedAt = Instant.now();
    this.categoriesSynced = categoriesSynced;
    this.productsSynced = productsSynced;
    this.productsDeactivated = productsDeactivated;
  }

  /** Transitions to FAILED with an error message. */
  public void fail(String errorMessage) {
    if (this.status != SyncStatus.RUNNING) {
      throw new IllegalStateException("SyncRun is not RUNNING — cannot fail");
    }
    this.status = SyncStatus.FAILED;
    this.finishedAt = Instant.now();
    this.errorMessage = errorMessage;
  }

  // -------------------------------------------------------------------------
  // Getters
  // -------------------------------------------------------------------------

  public SyncRunId getId() {
    return id;
  }

  public SupermarketId getSupermarketId() {
    return supermarketId;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public SyncStatus getStatus() {
    return status;
  }

  public int getCategoriesSynced() {
    return categoriesSynced;
  }

  public int getProductsSynced() {
    return productsSynced;
  }

  public int getProductsDeactivated() {
    return productsDeactivated;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
