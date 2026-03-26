package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — storage instructions (maps to API field {@code details.storage_instructions}).
 * Nullable, max 500.
 */
public record StorageInstructions(String value) {

  public StorageInstructions {
    if (value != null && value.length() > 500) {
      throw new IllegalArgumentException(
          "StorageInstructions cannot exceed 500 characters, got: " + value.length());
    }
  }

  public static StorageInstructions of(String value) {
    return new StorageInstructions(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
