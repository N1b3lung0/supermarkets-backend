package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — legal name of a Product (maps to API field {@code details.legal_name}). Nullable —
 * many products don't have a legal name separate from the display name.
 */
public record LegalName(String value) {

  public LegalName {
    if (value != null && value.length() > 255) {
      throw new IllegalArgumentException(
          "LegalName cannot exceed 255 characters, got: " + value.length());
    }
  }

  public static LegalName of(String value) {
    return new LegalName(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
