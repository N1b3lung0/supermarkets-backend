package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * JPA entity for the products table. Suppliers are stored in a separate product_suppliers table.
 */
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class ProductEntity {

  @Id private UUID id;

  @Column(name = "external_id", nullable = false, length = 50)
  private String externalId;

  @Column(name = "supermarket_id", nullable = false)
  private UUID supermarketId;

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(name = "legal_name", length = 255)
  private String legalName;

  @Column(length = 2000)
  private String description;

  @Column(length = 255)
  private String brand;

  @Column(length = 30)
  private String ean;

  @Column(length = 500)
  private String origin;

  @Column(length = 100)
  private String packaging;

  @Column(name = "thumbnail_url", length = 1000)
  private String thumbnailUrl;

  @Column(name = "storage_instructions", length = 500)
  private String storageInstructions;

  @Column(name = "usage_instructions", length = 500)
  private String usageInstructions;

  @Column(name = "mandatory_mentions", length = 1000)
  private String mandatoryMentions;

  @Column(name = "production_variant", length = 500)
  private String productionVariant;

  @Column(name = "danger_mentions", length = 1000)
  private String dangerMentions;

  @Column(length = 2000)
  private String allergens;

  @Column(length = 2000)
  private String ingredients;

  @ElementCollection
  @CollectionTable(name = "product_suppliers", joinColumns = @JoinColumn(name = "product_id"))
  @Column(name = "name", nullable = false, length = 255)
  @OrderColumn(name = "position")
  private List<String> suppliers = new ArrayList<>();

  @Column(name = "is_water", nullable = false)
  private boolean isWater;

  @Column(name = "requires_age_check", nullable = false)
  private boolean requiresAgeCheck;

  @Column(name = "is_bulk", nullable = false)
  private boolean isBulk;

  @Column(name = "is_variable_weight", nullable = false)
  private boolean isVariableWeight;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "purchase_limit", nullable = false)
  private int purchaseLimit;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public ProductEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public UUID getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLegalName() {
    return legalName;
  }

  public void setLegalName(String legalName) {
    this.legalName = legalName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getEan() {
    return ean;
  }

  public void setEan(String ean) {
    this.ean = ean;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getStorageInstructions() {
    return storageInstructions;
  }

  public void setStorageInstructions(String storageInstructions) {
    this.storageInstructions = storageInstructions;
  }

  public String getUsageInstructions() {
    return usageInstructions;
  }

  public void setUsageInstructions(String usageInstructions) {
    this.usageInstructions = usageInstructions;
  }

  public String getMandatoryMentions() {
    return mandatoryMentions;
  }

  public void setMandatoryMentions(String mandatoryMentions) {
    this.mandatoryMentions = mandatoryMentions;
  }

  public String getProductionVariant() {
    return productionVariant;
  }

  public void setProductionVariant(String productionVariant) {
    this.productionVariant = productionVariant;
  }

  public String getDangerMentions() {
    return dangerMentions;
  }

  public void setDangerMentions(String dangerMentions) {
    this.dangerMentions = dangerMentions;
  }

  public String getAllergens() {
    return allergens;
  }

  public void setAllergens(String allergens) {
    this.allergens = allergens;
  }

  public String getIngredients() {
    return ingredients;
  }

  public void setIngredients(String ingredients) {
    this.ingredients = ingredients;
  }

  public List<String> getSuppliers() {
    return suppliers;
  }

  public void setSuppliers(List<String> suppliers) {
    this.suppliers = suppliers;
  }

  public boolean isWater() {
    return isWater;
  }

  public void setWater(boolean isWater) {
    this.isWater = isWater;
  }

  public boolean isRequiresAgeCheck() {
    return requiresAgeCheck;
  }

  public void setRequiresAgeCheck(boolean requiresAgeCheck) {
    this.requiresAgeCheck = requiresAgeCheck;
  }

  public boolean isBulk() {
    return isBulk;
  }

  public void setBulk(boolean isBulk) {
    this.isBulk = isBulk;
  }

  public boolean isVariableWeight() {
    return isVariableWeight;
  }

  public void setVariableWeight(boolean isVariableWeight) {
    this.isVariableWeight = isVariableWeight;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public int getPurchaseLimit() {
    return purchaseLimit;
  }

  public void setPurchaseLimit(int purchaseLimit) {
    this.purchaseLimit = purchaseLimit;
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
