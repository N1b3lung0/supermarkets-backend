package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — packaging type (maps to API field {@code packaging}, e.g. "Bandeja", "Bolsa").
 * Nullable, max 100 characters.
 */
public record Packaging(String value) {

  public Packaging {
    if (value != null && value.length() > 100) {
      throw new IllegalArgumentException(
          "Packaging cannot exceed 100 characters, got: " + value.length());
    }
  }

  public static Packaging of(String value) {
    return new Packaging(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
