package com.n1b3lung0.supermarkets.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.n1b3lung0.supermarkets.product.domain.event.ProductDeactivated;
import com.n1b3lung0.supermarkets.product.domain.event.ProductPriceRecorded;
import com.n1b3lung0.supermarkets.product.domain.event.ProductSynced;
import com.n1b3lung0.supermarkets.product.domain.model.Allergens;
import com.n1b3lung0.supermarkets.product.domain.model.Brand;
import com.n1b3lung0.supermarkets.product.domain.model.Ean;
import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.product.domain.model.LegalName;
import com.n1b3lung0.supermarkets.product.domain.model.ProductBadges;
import com.n1b3lung0.supermarkets.product.domain.model.ProductName;
import com.n1b3lung0.supermarkets.product.domain.model.SellingMethod;
import com.n1b3lung0.supermarkets.product.domain.model.Supplier;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductDomainTest {

  // -------------------------------------------------------------------------
  // Value Object tests
  // -------------------------------------------------------------------------

  @Test
  void productName_shouldRejectBlank() {
    assertThatThrownBy(() -> ProductName.of("  ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void productName_shouldRejectOver255() {
    assertThatThrownBy(() -> ProductName.of("x".repeat(256)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void externalProductId_shouldRejectBlank() {
    assertThatThrownBy(() -> ExternalProductId.of("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void externalProductId_intFactory_shouldWork() {
    assertThat(ExternalProductId.of(42).value()).isEqualTo("42");
  }

  @Test
  void legalName_shouldAllowNull() {
    assertThat(LegalName.of(null).value()).isNull();
  }

  @Test
  void brand_shouldAllowNull() {
    assertThat(Brand.of(null).value()).isNull();
  }

  @Test
  void ean_shouldAllowNull() {
    assertThat(Ean.of(null).value()).isNull();
  }

  @Test
  void allergens_shouldRejectOver2000() {
    assertThatThrownBy(() -> Allergens.of("x".repeat(2001)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void supplier_shouldRejectBlankName() {
    assertThatThrownBy(() -> Supplier.of("  ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void productBadges_none_shouldBothBeFalse() {
    var badges = ProductBadges.none();
    assertThat(badges.isWater()).isFalse();
    assertThat(badges.requiresAgeCheck()).isFalse();
  }

  // -------------------------------------------------------------------------
  // Money tests
  // -------------------------------------------------------------------------

  @Test
  void money_shouldRejectNegativeAmount() {
    assertThatThrownBy(() -> Money.ofEur(new BigDecimal("-0.01")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void money_shouldRejectInvalidCurrency() {
    assertThatThrownBy(() -> Money.of(BigDecimal.ONE, "EURO"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void money_add_shouldSumCorrectly() {
    var a = Money.ofEur("1.00");
    var b = Money.ofEur("2.50");
    assertThat(a.add(b).amount()).isEqualByComparingTo("3.50");
  }

  @Test
  void money_add_shouldThrowOnCurrencyMismatch() {
    var eur = Money.ofEur("1.00");
    var usd = Money.of(BigDecimal.ONE, "USD");
    assertThatThrownBy(() -> eur.add(usd)).isInstanceOf(Money.CurrencyMismatchException.class);
  }

  // -------------------------------------------------------------------------
  // SellingMethod tests
  // -------------------------------------------------------------------------

  @Test
  void sellingMethod_fromCode_shouldMapKnownCodes() {
    assertThat(SellingMethod.fromCode(0)).isEqualTo(SellingMethod.UNIT);
    assertThat(SellingMethod.fromCode(2)).isEqualTo(SellingMethod.WEIGHT);
  }

  @Test
  void sellingMethod_fromCode_unknownDefaultsToUnit() {
    assertThat(SellingMethod.fromCode(99)).isEqualTo(SellingMethod.UNIT);
  }

  // -------------------------------------------------------------------------
  // Product aggregate tests
  // -------------------------------------------------------------------------

  @Test
  void product_create_shouldEmitProductSynced() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    var events = product.pullDomainEvents();

    assertThat(events).hasSize(1);
    assertThat(events.getFirst()).isInstanceOf(ProductSynced.class);
  }

  @Test
  void product_create_nullExternalId_shouldThrow() {
    assertThatThrownBy(
            () ->
                com.n1b3lung0.supermarkets.product.domain.model.Product.create(
                    null,
                    ProductMother.DEFAULT_SUPERMARKET,
                    ProductMother.DEFAULT_CATEGORY,
                    ProductName.of("Test"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    0))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void product_create_nullName_shouldThrow() {
    assertThatThrownBy(
            () ->
                com.n1b3lung0.supermarkets.product.domain.model.Product.create(
                    ExternalProductId.of("X"),
                    ProductMother.DEFAULT_SUPERMARKET,
                    ProductMother.DEFAULT_CATEGORY,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    0))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void product_update_changedName_shouldEmitProductSynced() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    product.pullDomainEvents(); // clear create event

    product.update(
        product.getCategoryId(),
        ProductName.of("Leche Semi"),
        product.getLegalName(),
        product.getDescription(),
        product.getBrand(),
        product.getEan(),
        product.getOrigin(),
        product.getPackaging(),
        product.getThumbnailUrl(),
        product.getStorageInstructions(),
        product.getUsageInstructions(),
        product.getMandatoryMentions(),
        product.getProductionVariant(),
        product.getDangerMentions(),
        product.getAllergens(),
        product.getIngredients(),
        List.of(),
        product.getBadges(),
        product.isBulk(),
        product.isVariableWeight(),
        product.getPurchaseLimit());

    assertThat(product.pullDomainEvents()).hasSize(1).first().isInstanceOf(ProductSynced.class);
    assertThat(product.getName().value()).isEqualTo("Leche Semi");
  }

  @Test
  void product_update_noChanges_shouldNotEmitEvent() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    product.pullDomainEvents();

    product.update(
        product.getCategoryId(),
        product.getName(),
        product.getLegalName(),
        product.getDescription(),
        product.getBrand(),
        product.getEan(),
        product.getOrigin(),
        product.getPackaging(),
        product.getThumbnailUrl(),
        product.getStorageInstructions(),
        product.getUsageInstructions(),
        product.getMandatoryMentions(),
        product.getProductionVariant(),
        product.getDangerMentions(),
        product.getAllergens(),
        product.getIngredients(),
        List.of(),
        product.getBadges(),
        product.isBulk(),
        product.isVariableWeight(),
        product.getPurchaseLimit());

    assertThat(product.pullDomainEvents()).isEmpty();
  }

  @Test
  void product_deactivate_shouldEmitProductDeactivated() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    product.pullDomainEvents();

    product.deactivate();
    assertThat(product.pullDomainEvents())
        .hasSize(1)
        .first()
        .isInstanceOf(ProductDeactivated.class);
    assertThat(product.isActive()).isFalse();
  }

  @Test
  void product_deactivate_alreadyInactive_shouldBeNoOp() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    product.deactivate();
    product.pullDomainEvents(); // clear first deactivation event

    product.deactivate(); // second call
    assertThat(product.pullDomainEvents()).isEmpty();
  }

  @Test
  void productPrice_create_shouldEmitProductPriceRecorded() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    var price = ProductMother.simpleProductPrice(product.getId());
    var events = price.pullDomainEvents();

    assertThat(events).hasSize(1);
    assertThat(events.getFirst()).isInstanceOf(ProductPriceRecorded.class);
    var event = (ProductPriceRecorded) events.getFirst();
    assertThat(event.unitPrice().amount()).isEqualByComparingTo("1.29");
  }
}
