package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — product brand (maps to API fields {@code brand} or {@code details.brand}).
 * Nullable, max 255 characters.
 */
public record Brand(String value) {

  public Brand {
    if (value != null && value.length() > 255) {
      throw new IllegalArgumentException(
          "Brand cannot exceed 255 characters, got: " + value.length());
    }
  }

  public static Brand of(String value) {
    return new Brand(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
