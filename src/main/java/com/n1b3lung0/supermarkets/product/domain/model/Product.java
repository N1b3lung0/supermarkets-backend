package com.n1b3lung0.supermarkets.product.domain.model;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.product.domain.event.ProductDeactivated;
import com.n1b3lung0.supermarkets.product.domain.event.ProductEvent;
import com.n1b3lung0.supermarkets.product.domain.event.ProductSynced;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root for the Product bounded context.
 *
 * <p>A Product belongs to exactly one Supermarket and one leaf Category. Price history is tracked
 * via the separate {@link ProductPrice} aggregate.
 */
public class Product {

  private final ProductId id;
  private final ExternalProductId externalId;
  private final SupermarketId supermarketId;
  private CategoryId categoryId;

  // Core fields
  private ProductName name;
  private LegalName legalName;
  private ProductDescription description;
  private Brand brand;
  private Ean ean;

  // Detail fields
  private ProductOrigin origin;
  private Packaging packaging;
  private ProductThumbnailUrl thumbnailUrl;
  private StorageInstructions storageInstructions;
  private UsageInstructions usageInstructions;
  private MandatoryMentions mandatoryMentions;
  private ProductionVariant productionVariant;
  private DangerMentions dangerMentions;
  private Allergens allergens;
  private Ingredients ingredients;
  private List<Supplier> suppliers;

  // Flags
  private ProductBadges badges;
  private boolean isBulk;
  private boolean isVariableWeight;
  private boolean isActive;
  private int purchaseLimit;

  private final List<ProductEvent> domainEvents = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Factory — business entry point
  // -------------------------------------------------------------------------

  public static Product create(
      ExternalProductId externalId,
      SupermarketId supermarketId,
      CategoryId categoryId,
      ProductName name,
      LegalName legalName,
      ProductDescription description,
      Brand brand,
      Ean ean,
      ProductOrigin origin,
      Packaging packaging,
      ProductThumbnailUrl thumbnailUrl,
      StorageInstructions storageInstructions,
      UsageInstructions usageInstructions,
      MandatoryMentions mandatoryMentions,
      ProductionVariant productionVariant,
      DangerMentions dangerMentions,
      Allergens allergens,
      Ingredients ingredients,
      List<Supplier> suppliers,
      ProductBadges badges,
      boolean isBulk,
      boolean isVariableWeight,
      int purchaseLimit) {

    Objects.requireNonNull(externalId, "externalId is required");
    Objects.requireNonNull(supermarketId, "supermarketId is required");
    Objects.requireNonNull(categoryId, "categoryId is required");
    Objects.requireNonNull(name, "name is required");

    var product =
        new Product(
            ProductId.generate(),
            externalId,
            supermarketId,
            categoryId,
            name,
            legalName,
            description,
            brand,
            ean,
            origin,
            packaging,
            thumbnailUrl,
            storageInstructions,
            usageInstructions,
            mandatoryMentions,
            productionVariant,
            dangerMentions,
            allergens,
            ingredients,
            suppliers != null ? List.copyOf(suppliers) : List.of(),
            badges != null ? badges : ProductBadges.none(),
            isBulk,
            isVariableWeight,
            true,
            purchaseLimit);

    product.domainEvents.add(
        new ProductSynced(product.id, product.externalId, product.supermarketId, Instant.now()));
    return product;
  }

  // -------------------------------------------------------------------------
  // Reconstitution factory — persistence only, no events
  // -------------------------------------------------------------------------

  public static Product reconstitute(
      ProductId id,
      ExternalProductId externalId,
      SupermarketId supermarketId,
      CategoryId categoryId,
      ProductName name,
      LegalName legalName,
      ProductDescription description,
      Brand brand,
      Ean ean,
      ProductOrigin origin,
      Packaging packaging,
      ProductThumbnailUrl thumbnailUrl,
      StorageInstructions storageInstructions,
      UsageInstructions usageInstructions,
      MandatoryMentions mandatoryMentions,
      ProductionVariant productionVariant,
      DangerMentions dangerMentions,
      Allergens allergens,
      Ingredients ingredients,
      List<Supplier> suppliers,
      ProductBadges badges,
      boolean isBulk,
      boolean isVariableWeight,
      boolean isActive,
      int purchaseLimit) {

    return new Product(
        id,
        externalId,
        supermarketId,
        categoryId,
        name,
        legalName,
        description,
        brand,
        ean,
        origin,
        packaging,
        thumbnailUrl,
        storageInstructions,
        usageInstructions,
        mandatoryMentions,
        productionVariant,
        dangerMentions,
        allergens,
        ingredients,
        suppliers != null ? List.copyOf(suppliers) : List.of(),
        badges != null ? badges : ProductBadges.none(),
        isBulk,
        isVariableWeight,
        isActive,
        purchaseLimit);
  }

  private Product(
      ProductId id,
      ExternalProductId externalId,
      SupermarketId supermarketId,
      CategoryId categoryId,
      ProductName name,
      LegalName legalName,
      ProductDescription description,
      Brand brand,
      Ean ean,
      ProductOrigin origin,
      Packaging packaging,
      ProductThumbnailUrl thumbnailUrl,
      StorageInstructions storageInstructions,
      UsageInstructions usageInstructions,
      MandatoryMentions mandatoryMentions,
      ProductionVariant productionVariant,
      DangerMentions dangerMentions,
      Allergens allergens,
      Ingredients ingredients,
      List<Supplier> suppliers,
      ProductBadges badges,
      boolean isBulk,
      boolean isVariableWeight,
      boolean isActive,
      int purchaseLimit) {

    this.id = id;
    this.externalId = externalId;
    this.supermarketId = supermarketId;
    this.categoryId = categoryId;
    this.name = name;
    this.legalName = legalName;
    this.description = description;
    this.brand = brand;
    this.ean = ean;
    this.origin = origin;
    this.packaging = packaging;
    this.thumbnailUrl = thumbnailUrl;
    this.storageInstructions = storageInstructions;
    this.usageInstructions = usageInstructions;
    this.mandatoryMentions = mandatoryMentions;
    this.productionVariant = productionVariant;
    this.dangerMentions = dangerMentions;
    this.allergens = allergens;
    this.ingredients = ingredients;
    this.suppliers = suppliers;
    this.badges = badges;
    this.isBulk = isBulk;
    this.isVariableWeight = isVariableWeight;
    this.isActive = isActive;
    this.purchaseLimit = purchaseLimit;
  }

  // -------------------------------------------------------------------------
  // Domain behaviour
  // -------------------------------------------------------------------------

  /**
   * Updates all mutable product fields from scraper data. Emits {@link ProductSynced} only when at
   * least one field actually changed.
   */
  public void update(
      CategoryId newCategoryId,
      ProductName newName,
      LegalName newLegalName,
      ProductDescription newDescription,
      Brand newBrand,
      Ean newEan,
      ProductOrigin newOrigin,
      Packaging newPackaging,
      ProductThumbnailUrl newThumbnailUrl,
      StorageInstructions newStorageInstructions,
      UsageInstructions newUsageInstructions,
      MandatoryMentions newMandatoryMentions,
      ProductionVariant newProductionVariant,
      DangerMentions newDangerMentions,
      Allergens newAllergens,
      Ingredients newIngredients,
      List<Supplier> newSuppliers,
      ProductBadges newBadges,
      boolean newIsBulk,
      boolean newIsVariableWeight,
      int newPurchaseLimit) {

    boolean changed = false;

    if (!Objects.equals(this.categoryId, newCategoryId)) {
      this.categoryId = newCategoryId;
      changed = true;
    }
    if (!Objects.equals(this.name, newName)) {
      this.name = newName;
      changed = true;
    }
    if (!Objects.equals(this.legalName, newLegalName)) {
      this.legalName = newLegalName;
      changed = true;
    }
    if (!Objects.equals(this.description, newDescription)) {
      this.description = newDescription;
      changed = true;
    }
    if (!Objects.equals(this.brand, newBrand)) {
      this.brand = newBrand;
      changed = true;
    }
    if (!Objects.equals(this.ean, newEan)) {
      this.ean = newEan;
      changed = true;
    }
    if (!Objects.equals(this.origin, newOrigin)) {
      this.origin = newOrigin;
      changed = true;
    }
    if (!Objects.equals(this.packaging, newPackaging)) {
      this.packaging = newPackaging;
      changed = true;
    }
    if (!Objects.equals(this.thumbnailUrl, newThumbnailUrl)) {
      this.thumbnailUrl = newThumbnailUrl;
      changed = true;
    }
    if (!Objects.equals(this.storageInstructions, newStorageInstructions)) {
      this.storageInstructions = newStorageInstructions;
      changed = true;
    }
    if (!Objects.equals(this.usageInstructions, newUsageInstructions)) {
      this.usageInstructions = newUsageInstructions;
      changed = true;
    }
    if (!Objects.equals(this.mandatoryMentions, newMandatoryMentions)) {
      this.mandatoryMentions = newMandatoryMentions;
      changed = true;
    }
    if (!Objects.equals(this.productionVariant, newProductionVariant)) {
      this.productionVariant = newProductionVariant;
      changed = true;
    }
    if (!Objects.equals(this.dangerMentions, newDangerMentions)) {
      this.dangerMentions = newDangerMentions;
      changed = true;
    }
    if (!Objects.equals(this.allergens, newAllergens)) {
      this.allergens = newAllergens;
      changed = true;
    }
    if (!Objects.equals(this.ingredients, newIngredients)) {
      this.ingredients = newIngredients;
      changed = true;
    }
    var normalizedSuppliers =
        newSuppliers != null ? List.copyOf(newSuppliers) : List.<Supplier>of();
    if (!Objects.equals(this.suppliers, normalizedSuppliers)) {
      this.suppliers = normalizedSuppliers;
      changed = true;
    }
    if (!Objects.equals(this.badges, newBadges)) {
      this.badges = newBadges != null ? newBadges : ProductBadges.none();
      changed = true;
    }
    if (this.isBulk != newIsBulk) {
      this.isBulk = newIsBulk;
      changed = true;
    }
    if (this.isVariableWeight != newIsVariableWeight) {
      this.isVariableWeight = newIsVariableWeight;
      changed = true;
    }
    if (this.purchaseLimit != newPurchaseLimit) {
      this.purchaseLimit = newPurchaseLimit;
      changed = true;
    }

    if (changed) {
      this.isActive = true; // re-activate if it was soft-deactivated
      domainEvents.add(new ProductSynced(id, externalId, supermarketId, Instant.now()));
    }
  }

  /**
   * Marks the product as inactive (soft deactivation). No-op if already inactive — prevents
   * duplicate events.
   */
  public void deactivate() {
    if (!isActive) {
      return;
    }
    isActive = false;
    domainEvents.add(new ProductDeactivated(id, supermarketId, Instant.now()));
  }

  // -------------------------------------------------------------------------
  // Domain events
  // -------------------------------------------------------------------------

  public List<ProductEvent> pullDomainEvents() {
    var events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }

  // -------------------------------------------------------------------------
  // Accessors
  // -------------------------------------------------------------------------

  public ProductId getId() {
    return id;
  }

  public ExternalProductId getExternalId() {
    return externalId;
  }

  public SupermarketId getSupermarketId() {
    return supermarketId;
  }

  public CategoryId getCategoryId() {
    return categoryId;
  }

  public ProductName getName() {
    return name;
  }

  public LegalName getLegalName() {
    return legalName;
  }

  public ProductDescription getDescription() {
    return description;
  }

  public Brand getBrand() {
    return brand;
  }

  public Ean getEan() {
    return ean;
  }

  public ProductOrigin getOrigin() {
    return origin;
  }

  public Packaging getPackaging() {
    return packaging;
  }

  public ProductThumbnailUrl getThumbnailUrl() {
    return thumbnailUrl;
  }

  public StorageInstructions getStorageInstructions() {
    return storageInstructions;
  }

  public UsageInstructions getUsageInstructions() {
    return usageInstructions;
  }

  public MandatoryMentions getMandatoryMentions() {
    return mandatoryMentions;
  }

  public ProductionVariant getProductionVariant() {
    return productionVariant;
  }

  public DangerMentions getDangerMentions() {
    return dangerMentions;
  }

  public Allergens getAllergens() {
    return allergens;
  }

  public Ingredients getIngredients() {
    return ingredients;
  }

  public List<Supplier> getSuppliers() {
    return Collections.unmodifiableList(suppliers);
  }

  public ProductBadges getBadges() {
    return badges;
  }

  public boolean isBulk() {
    return isBulk;
  }

  public boolean isVariableWeight() {
    return isVariableWeight;
  }

  public boolean isActive() {
    return isActive;
  }

  public int getPurchaseLimit() {
    return purchaseLimit;
  }
}
