package com.n1b3lung0.supermarkets.comparison.domain.model;

import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a single product found during a comparison search. Carries the latest
 * known price for that product at the time of comparison.
 */
public record ProductMatch(
    UUID productId,
    UUID supermarketId,
    String supermarketName,
    String productName,
    Money unitPrice,
    Money bulkPrice,
    Money referencePrice,
    String referenceFormat,
    Instant priceRecordedAt) {

  public ProductMatch {
    Objects.requireNonNull(productId, "productId is required");
    Objects.requireNonNull(supermarketId, "supermarketId is required");
    Objects.requireNonNull(supermarketName, "supermarketName is required");
    Objects.requireNonNull(productName, "productName is required");
    Objects.requireNonNull(unitPrice, "unitPrice is required");
    Objects.requireNonNull(priceRecordedAt, "priceRecordedAt is required");
  }

  /** Convenience: raw unit price amount for sorting without unpacking the Money record. */
  public BigDecimal unitPriceAmount() {
    return unitPrice.amount();
  }
}
