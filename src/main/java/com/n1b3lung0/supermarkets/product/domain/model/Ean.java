package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — EAN barcode (EAN-13 or EAN-8) of a product (maps to API field {@code ean}).
 * Nullable, max 30 characters.
 */
public record Ean(String value) {

  public Ean {
    if (value != null && value.length() > 30) {
      throw new IllegalArgumentException("Ean cannot exceed 30 characters, got: " + value.length());
    }
  }

  public static Ean of(String value) {
    return new Ean(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
