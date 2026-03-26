package com.n1b3lung0.supermarkets.product.domain;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.product.domain.model.Allergens;
import com.n1b3lung0.supermarkets.product.domain.model.Brand;
import com.n1b3lung0.supermarkets.product.domain.model.DangerMentions;
import com.n1b3lung0.supermarkets.product.domain.model.Ean;
import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.product.domain.model.Ingredients;
import com.n1b3lung0.supermarkets.product.domain.model.LegalName;
import com.n1b3lung0.supermarkets.product.domain.model.MandatoryMentions;
import com.n1b3lung0.supermarkets.product.domain.model.Packaging;
import com.n1b3lung0.supermarkets.product.domain.model.PriceInstructions;
import com.n1b3lung0.supermarkets.product.domain.model.Product;
import com.n1b3lung0.supermarkets.product.domain.model.ProductBadges;
import com.n1b3lung0.supermarkets.product.domain.model.ProductDescription;
import com.n1b3lung0.supermarkets.product.domain.model.ProductName;
import com.n1b3lung0.supermarkets.product.domain.model.ProductOrigin;
import com.n1b3lung0.supermarkets.product.domain.model.ProductPrice;
import com.n1b3lung0.supermarkets.product.domain.model.ProductThumbnailUrl;
import com.n1b3lung0.supermarkets.product.domain.model.ProductionVariant;
import com.n1b3lung0.supermarkets.product.domain.model.StorageInstructions;
import com.n1b3lung0.supermarkets.product.domain.model.UsageInstructions;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;
import java.util.UUID;

/** ObjectMother — provides ready-made Product instances for tests. */
public final class ProductMother {

  private ProductMother() {}

  public static final SupermarketId DEFAULT_SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  public static final CategoryId DEFAULT_CATEGORY = CategoryId.generate();

  public static Product simpleProduct(SupermarketId supermarketId, CategoryId categoryId) {
    return Product.create(
        ExternalProductId.of("3400"),
        supermarketId,
        categoryId,
        ProductName.of("Leche Entera"),
        LegalName.of("Leche Entera UHT"),
        ProductDescription.of("Leche entera de calidad"),
        Brand.of("Hacendado"),
        Ean.of("8410011015827"),
        ProductOrigin.of("España"),
        Packaging.of("Brik"),
        ProductThumbnailUrl.of("https://prod.static9.net.au/fs/1234.jpg"),
        StorageInstructions.of("Conservar en lugar fresco"),
        UsageInstructions.of(null),
        MandatoryMentions.of(null),
        ProductionVariant.of(null),
        DangerMentions.of(null),
        Allergens.of("Contiene lactosa"),
        Ingredients.of("Leche entera"),
        List.of(),
        ProductBadges.none(),
        false,
        false,
        0);
  }

  public static Product simpleProductWithExternalId(
      SupermarketId supermarketId, CategoryId categoryId, String externalId) {
    return Product.create(
        ExternalProductId.of(externalId),
        supermarketId,
        categoryId,
        ProductName.of("Leche Entera"),
        LegalName.of(null),
        ProductDescription.of(null),
        Brand.of("Hacendado"),
        Ean.of(null),
        ProductOrigin.of(null),
        Packaging.of(null),
        ProductThumbnailUrl.of(null),
        StorageInstructions.of(null),
        UsageInstructions.of(null),
        MandatoryMentions.of(null),
        ProductionVariant.of(null),
        DangerMentions.of(null),
        Allergens.of(null),
        Ingredients.of(null),
        List.of(),
        ProductBadges.none(),
        false,
        false,
        0);
  }

  public static PriceInstructions simplePriceInstructions() {
    return PriceInstructions.unitOnly(Money.ofEur("1.29"));
  }

  public static ProductPrice simpleProductPrice(
      com.n1b3lung0.supermarkets.product.domain.model.ProductId productId) {
    return ProductPrice.create(productId, simplePriceInstructions());
  }
}
