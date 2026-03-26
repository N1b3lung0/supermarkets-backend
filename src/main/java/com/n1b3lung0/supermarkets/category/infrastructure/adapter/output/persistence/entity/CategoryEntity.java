package com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * JPA entity for the categories table. Level is stored as a discriminator string + nullable
 * parent_id.
 */
@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
public class CategoryEntity {

  @Id private UUID id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(name = "external_id", nullable = false, length = 50)
  private String externalId;

  @Column(name = "supermarket_id", nullable = false)
  private UUID supermarketId;

  /** Discriminator: TOP | SUB | LEAF */
  @Column(name = "level_type", nullable = false, length = 10)
  private String levelType;

  /** Null for TOP-level categories. */
  @Column(name = "parent_id")
  private UUID parentId;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public CategoryEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public UUID getSupermarketId() {
    return supermarketId;
  }

  public void setSupermarketId(UUID supermarketId) {
    this.supermarketId = supermarketId;
  }

  public String getLevelType() {
    return levelType;
  }

  public void setLevelType(String levelType) {
    this.levelType = levelType;
  }

  public UUID getParentId() {
    return parentId;
  }

  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}
