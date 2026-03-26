package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — product description (maps to API field {@code details.description}). Nullable, max
 * 2000 characters.
 */
public record ProductDescription(String value) {

  public ProductDescription {
    if (value != null && value.length() > 2000) {
      throw new IllegalArgumentException(
          "ProductDescription cannot exceed 2000 characters, got: " + value.length());
    }
  }

  public static ProductDescription of(String value) {
    return new ProductDescription(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
