package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — production variant note (maps to API field {@code details.production_variant}).
 * Nullable, max 500.
 */
public record ProductionVariant(String value) {

  public ProductionVariant {
    if (value != null && value.length() > 500) {
      throw new IllegalArgumentException(
          "ProductionVariant cannot exceed 500 characters, got: " + value.length());
    }
  }

  public static ProductionVariant of(String value) {
    return new ProductionVariant(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
