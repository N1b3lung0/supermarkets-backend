package com.n1b3lung0.supermarkets.product.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Value Object representing the unique identity of a ProductPrice snapshot. */
public record ProductPriceId(UUID value) {

  public ProductPriceId {
    Objects.requireNonNull(value, "ProductPriceId value is required");
  }

  public static ProductPriceId generate() {
    return new ProductPriceId(UUID.randomUUID());
  }

  public static ProductPriceId of(UUID value) {
    return new ProductPriceId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
