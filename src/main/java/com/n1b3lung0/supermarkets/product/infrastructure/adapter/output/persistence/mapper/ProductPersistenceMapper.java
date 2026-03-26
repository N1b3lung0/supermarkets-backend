package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import com.n1b3lung0.supermarkets.product.application.dto.SupplierView;
import com.n1b3lung0.supermarkets.product.domain.model.Allergens;
import com.n1b3lung0.supermarkets.product.domain.model.Brand;
import com.n1b3lung0.supermarkets.product.domain.model.DangerMentions;
import com.n1b3lung0.supermarkets.product.domain.model.Ean;
import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.product.domain.model.Ingredients;
import com.n1b3lung0.supermarkets.product.domain.model.LegalName;
import com.n1b3lung0.supermarkets.product.domain.model.MandatoryMentions;
import com.n1b3lung0.supermarkets.product.domain.model.Packaging;
import com.n1b3lung0.supermarkets.product.domain.model.Product;
import com.n1b3lung0.supermarkets.product.domain.model.ProductBadges;
import com.n1b3lung0.supermarkets.product.domain.model.ProductDescription;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.product.domain.model.ProductName;
import com.n1b3lung0.supermarkets.product.domain.model.ProductOrigin;
import com.n1b3lung0.supermarkets.product.domain.model.ProductThumbnailUrl;
import com.n1b3lung0.supermarkets.product.domain.model.ProductionVariant;
import com.n1b3lung0.supermarkets.product.domain.model.StorageInstructions;
import com.n1b3lung0.supermarkets.product.domain.model.Supplier;
import com.n1b3lung0.supermarkets.product.domain.model.UsageInstructions;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity.ProductEntity;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity.ProductPriceEntity;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;

/** Manual mapper: ProductEntity ↔ Product domain object and view projections. */
public class ProductPersistenceMapper {

  // -------------------------------------------------------------------------
  // Domain → Entity
  // -------------------------------------------------------------------------

  public ProductEntity toEntity(Product product) {
    var entity = new ProductEntity();
    entity.setId(product.getId().value());
    entity.setExternalId(product.getExternalId().value());
    entity.setSupermarketId(product.getSupermarketId().value());
    entity.setCategoryId(product.getCategoryId().value());
    entity.setName(product.getName().value());
    entity.setLegalName(product.getLegalName() != null ? product.getLegalName().value() : null);
    entity.setDescription(
        product.getDescription() != null ? product.getDescription().value() : null);
    entity.setBrand(product.getBrand() != null ? product.getBrand().value() : null);
    entity.setEan(product.getEan() != null ? product.getEan().value() : null);
    entity.setOrigin(product.getOrigin() != null ? product.getOrigin().value() : null);
    entity.setPackaging(product.getPackaging() != null ? product.getPackaging().value() : null);
    entity.setThumbnailUrl(
        product.getThumbnailUrl() != null ? product.getThumbnailUrl().value() : null);
    entity.setStorageInstructions(
        product.getStorageInstructions() != null ? product.getStorageInstructions().value() : null);
    entity.setUsageInstructions(
        product.getUsageInstructions() != null ? product.getUsageInstructions().value() : null);
    entity.setMandatoryMentions(
        product.getMandatoryMentions() != null ? product.getMandatoryMentions().value() : null);
    entity.setProductionVariant(
        product.getProductionVariant() != null ? product.getProductionVariant().value() : null);
    entity.setDangerMentions(
        product.getDangerMentions() != null ? product.getDangerMentions().value() : null);
    entity.setAllergens(product.getAllergens() != null ? product.getAllergens().value() : null);
    entity.setIngredients(
        product.getIngredients() != null ? product.getIngredients().value() : null);
    entity.setSuppliers(product.getSuppliers().stream().map(Supplier::name).toList());
    entity.setWater(product.getBadges().isWater());
    entity.setRequiresAgeCheck(product.getBadges().requiresAgeCheck());
    entity.setBulk(product.isBulk());
    entity.setVariableWeight(product.isVariableWeight());
    entity.setActive(product.isActive());
    entity.setPurchaseLimit(product.getPurchaseLimit());
    return entity;
  }

  // -------------------------------------------------------------------------
  // Entity → Domain
  // -------------------------------------------------------------------------

  public Product toDomain(ProductEntity entity) {
    return Product.reconstitute(
        ProductId.of(entity.getId()),
        ExternalProductId.of(entity.getExternalId()),
        SupermarketId.of(entity.getSupermarketId()),
        CategoryId.of(entity.getCategoryId()),
        ProductName.of(entity.getName()),
        LegalName.of(entity.getLegalName()),
        ProductDescription.of(entity.getDescription()),
        Brand.of(entity.getBrand()),
        Ean.of(entity.getEan()),
        ProductOrigin.of(entity.getOrigin()),
        Packaging.of(entity.getPackaging()),
        ProductThumbnailUrl.of(entity.getThumbnailUrl()),
        StorageInstructions.of(entity.getStorageInstructions()),
        UsageInstructions.of(entity.getUsageInstructions()),
        MandatoryMentions.of(entity.getMandatoryMentions()),
        ProductionVariant.of(entity.getProductionVariant()),
        DangerMentions.of(entity.getDangerMentions()),
        Allergens.of(entity.getAllergens()),
        Ingredients.of(entity.getIngredients()),
        entity.getSuppliers().stream().map(Supplier::of).toList(),
        ProductBadges.of(entity.isWater(), entity.isRequiresAgeCheck()),
        entity.isBulk(),
        entity.isVariableWeight(),
        entity.isActive(),
        entity.getPurchaseLimit());
  }

  // -------------------------------------------------------------------------
  // Entity → Summary view (no price join — used in list queries)
  // -------------------------------------------------------------------------

  public ProductSummaryView toSummaryView(ProductEntity entity) {
    return new ProductSummaryView(
        entity.getId(),
        entity.getExternalId(),
        entity.getName(),
        entity.getBrand(),
        entity.getThumbnailUrl(),
        null, // unitPrice — populated by JPA adapter when joining with latest price
        null, // bulkPrice
        entity.isActive(),
        entity.getSupermarketId(),
        entity.getCategoryId());
  }

  // -------------------------------------------------------------------------
  // Entity + latest price → Detail view
  // -------------------------------------------------------------------------

  public ProductDetailView toDetailView(ProductEntity entity, ProductPriceEntity latestPrice) {
    return new ProductDetailView(
        entity.getId(),
        entity.getExternalId(),
        entity.getSupermarketId(),
        entity.getCategoryId(),
        entity.getName(),
        entity.getLegalName(),
        entity.getDescription(),
        entity.getBrand(),
        entity.getEan(),
        entity.getOrigin(),
        entity.getPackaging(),
        entity.getThumbnailUrl(),
        entity.getStorageInstructions(),
        entity.getUsageInstructions(),
        entity.getMandatoryMentions(),
        entity.getProductionVariant(),
        entity.getDangerMentions(),
        entity.getAllergens(),
        entity.getIngredients(),
        entity.getSuppliers().stream().map(SupplierView::new).toList(),
        entity.isWater(),
        entity.isRequiresAgeCheck(),
        entity.isBulk(),
        entity.isVariableWeight(),
        entity.isActive(),
        entity.getPurchaseLimit(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        latestPrice != null ? toPriceView(latestPrice) : null);
  }

  // -------------------------------------------------------------------------
  // Price entity → view
  // -------------------------------------------------------------------------

  public ProductPriceView toPriceView(ProductPriceEntity entity) {
    var currency = entity.getCurrency() != null ? entity.getCurrency() : "EUR";
    return new ProductPriceView(
        entity.getId(),
        entity.getProductId(),
        Money.ofEur(entity.getUnitPrice()),
        entity.getBulkPrice() != null ? Money.of(entity.getBulkPrice(), currency) : null,
        entity.getReferencePrice() != null ? Money.of(entity.getReferencePrice(), currency) : null,
        entity.getReferenceFormat(),
        entity.getIva(),
        entity.isPriceDecreased(),
        entity.getPreviousUnitPrice() != null
            ? Money.of(entity.getPreviousUnitPrice(), currency)
            : null,
        entity.getRecordedAt());
  }

  // -------------------------------------------------------------------------
  // Product + price entity → Summary view (with prices populated)
  // -------------------------------------------------------------------------

  public ProductSummaryView toSummaryViewWithPrice(
      ProductEntity entity, ProductPriceEntity latestPrice) {
    var currency =
        latestPrice != null && latestPrice.getCurrency() != null
            ? latestPrice.getCurrency()
            : "EUR";
    return new ProductSummaryView(
        entity.getId(),
        entity.getExternalId(),
        entity.getName(),
        entity.getBrand(),
        entity.getThumbnailUrl(),
        latestPrice != null ? Money.of(latestPrice.getUnitPrice(), currency) : null,
        latestPrice != null && latestPrice.getBulkPrice() != null
            ? Money.of(latestPrice.getBulkPrice(), currency)
            : null,
        entity.isActive(),
        entity.getSupermarketId(),
        entity.getCategoryId());
  }

  /** Extracts just the supplier names from domain objects. */
  public List<String> toSupplierNames(List<Supplier> suppliers) {
    return suppliers.stream().map(Supplier::name).toList();
  }
}
