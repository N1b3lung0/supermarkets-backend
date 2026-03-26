package com.n1b3lung0.supermarkets.product.domain.model;

import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.util.Objects;

/**
 * Value Object — the complete {@code price_instructions} block from the Mercadona API. Stored as a
 * flat record; the full block is preserved in each {@code ProductPrice} history entry.
 */
public record PriceInstructions(
    Money unitPrice,
    Money bulkPrice,
    Money referencePrice,
    String referenceFormat,
    String sizeFormat,
    Double unitSize,
    String unitName,
    Integer totalUnits,
    Integer packSize,
    Integer iva,
    String taxPercentage,
    SellingMethod sellingMethod,
    boolean isNew,
    boolean isPack,
    boolean approxSize,
    boolean priceDecreased,
    boolean unitSelector,
    boolean bunchSelector,
    Money previousUnitPrice,
    Double minBunchAmount,
    Double incrementBunchAmount) {

  public PriceInstructions {
    Objects.requireNonNull(unitPrice, "unitPrice is required");
    Objects.requireNonNull(sellingMethod, "sellingMethod is required");
  }

  /** Minimal factory — unit-only product, no optional fields. */
  public static PriceInstructions unitOnly(Money unitPrice) {
    return new PriceInstructions(
        unitPrice,
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
        SellingMethod.UNIT,
        false,
        false,
        false,
        false,
        false,
        false,
        null,
        null,
        null);
  }
}
