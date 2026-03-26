package com.n1b3lung0.supermarkets.product.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Value Object representing the unique identity of a Product. */
public record ProductId(UUID value) {

  public ProductId {
    Objects.requireNonNull(value, "ProductId value is required");
  }

  public static ProductId generate() {
    return new ProductId(UUID.randomUUID());
  }

  public static ProductId of(UUID value) {
    return new ProductId(value);
  }

  public static ProductId of(String value) {
    return new ProductId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
