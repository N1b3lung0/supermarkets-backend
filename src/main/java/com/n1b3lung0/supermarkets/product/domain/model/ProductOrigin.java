package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — country/region of origin (maps to API field {@code origin}). Nullable, max 500
 * characters.
 */
public record ProductOrigin(String value) {

  public ProductOrigin {
    if (value != null && value.length() > 500) {
      throw new IllegalArgumentException(
          "ProductOrigin cannot exceed 500 characters, got: " + value.length());
    }
  }

  public static ProductOrigin of(String value) {
    return new ProductOrigin(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
