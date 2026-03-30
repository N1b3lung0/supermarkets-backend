package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for the sync_runs table. */
@Entity
@Table(name = "sync_runs")
public class SyncRunEntity {

  @Id private UUID id;

  @Column(name = "supermarket_id", nullable = false)
  private UUID supermarketId;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "categories_synced", nullable = false)
  private int categoriesSynced;

  @Column(name = "products_synced", nullable = false)
  private int productsSynced;

  @Column(name = "products_deactivated", nullable = false)
  private int productsDeactivated;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  /** Required by JPA. */
  public SyncRunEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getSupermarketId() {
    return supermarketId;
  }

  public void setSupermarketId(UUID supermarketId) {
    this.supermarketId = supermarketId;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(Instant finishedAt) {
    this.finishedAt = finishedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getCategoriesSynced() {
    return categoriesSynced;
  }

  public void setCategoriesSynced(int categoriesSynced) {
    this.categoriesSynced = categoriesSynced;
  }

  public int getProductsSynced() {
    return productsSynced;
  }

  public void setProductsSynced(int productsSynced) {
    this.productsSynced = productsSynced;
  }

  public int getProductsDeactivated() {
    return productsDeactivated;
  }

  public void setProductsDeactivated(int productsDeactivated) {
    this.productsDeactivated = productsDeactivated;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
