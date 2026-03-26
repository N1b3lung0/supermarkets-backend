package com.n1b3lung0.supermarkets.product.domain.model;

import java.util.Objects;

/** Value Object — display name of a Product (maps to API field {@code display_name}). */
public record ProductName(String value) {

  public ProductName {
    Objects.requireNonNull(value, "ProductName value is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException("ProductName cannot be blank");
    }
    if (value.length() > 255) {
      throw new IllegalArgumentException(
          "ProductName cannot exceed 255 characters, got: " + value.length());
    }
  }

  public static ProductName of(String value) {
    return new ProductName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
