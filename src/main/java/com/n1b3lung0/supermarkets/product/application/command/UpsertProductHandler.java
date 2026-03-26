package com.n1b3lung0.supermarkets.product.application.command;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.RecordProductPriceCommand;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.product.application.port.input.command.RecordProductPriceUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.command.UpsertProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
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
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;
import java.util.Objects;

/**
 * Handles the upsert product flow:
 *
 * <ol>
 *   <li>Create the product if it does not exist, otherwise update changed fields.
 *   <li>Always record a new price snapshot regardless of whether product data changed.
 * </ol>
 */
public class UpsertProductHandler implements UpsertProductUseCase {

  private final ProductRepositoryPort productRepository;
  private final RecordProductPriceUseCase recordPrice;

  public UpsertProductHandler(
      ProductRepositoryPort productRepository, RecordProductPriceUseCase recordPrice) {
    this.productRepository = productRepository;
    this.recordPrice = recordPrice;
  }

  @Override
  public ProductId execute(UpsertProductCommand command) {
    Objects.requireNonNull(command, "command is required");

    var externalId = ExternalProductId.of(command.externalId());
    var supermarketId = SupermarketId.of(command.supermarketId());

    var existing = productRepository.findByExternalIdAndSupermarket(externalId, supermarketId);
    Product product;

    if (existing.isEmpty()) {
      product =
          Product.create(
              externalId,
              supermarketId,
              CategoryId.of(command.categoryId()),
              ProductName.of(command.name()),
              LegalName.of(command.legalName()),
              ProductDescription.of(command.description()),
              Brand.of(command.brand()),
              Ean.of(command.ean()),
              ProductOrigin.of(command.origin()),
              Packaging.of(command.packaging()),
              ProductThumbnailUrl.of(command.thumbnailUrl()),
              StorageInstructions.of(command.storageInstructions()),
              UsageInstructions.of(command.usageInstructions()),
              MandatoryMentions.of(command.mandatoryMentions()),
              ProductionVariant.of(command.productionVariant()),
              DangerMentions.of(command.dangerMentions()),
              Allergens.of(command.allergens()),
              Ingredients.of(command.ingredients()),
              buildSuppliers(command.supplierNames()),
              ProductBadges.of(command.isWater(), command.requiresAgeCheck()),
              command.isBulk(),
              command.isVariableWeight(),
              command.purchaseLimit());
      productRepository.save(product);
    } else {
      product = existing.get();
      product.update(
          CategoryId.of(command.categoryId()),
          ProductName.of(command.name()),
          LegalName.of(command.legalName()),
          ProductDescription.of(command.description()),
          Brand.of(command.brand()),
          Ean.of(command.ean()),
          ProductOrigin.of(command.origin()),
          Packaging.of(command.packaging()),
          ProductThumbnailUrl.of(command.thumbnailUrl()),
          StorageInstructions.of(command.storageInstructions()),
          UsageInstructions.of(command.usageInstructions()),
          MandatoryMentions.of(command.mandatoryMentions()),
          ProductionVariant.of(command.productionVariant()),
          DangerMentions.of(command.dangerMentions()),
          Allergens.of(command.allergens()),
          Ingredients.of(command.ingredients()),
          buildSuppliers(command.supplierNames()),
          ProductBadges.of(command.isWater(), command.requiresAgeCheck()),
          command.isBulk(),
          command.isVariableWeight(),
          command.purchaseLimit());
      // Save only if domain emitted an event (i.e. something actually changed)
      if (!product.pullDomainEvents().isEmpty()) {
        productRepository.save(product);
      }
    }

    // Always record a price snapshot (daily history)
    recordPrice.execute(
        new RecordProductPriceCommand(product.getId(), command.priceInstructions()));
    return product.getId();
  }

  private List<Supplier> buildSuppliers(List<String> names) {
    if (names == null) {
      return List.of();
    }
    return names.stream().map(Supplier::of).toList();
  }
}
