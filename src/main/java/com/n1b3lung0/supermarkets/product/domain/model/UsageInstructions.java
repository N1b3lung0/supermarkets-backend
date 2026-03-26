package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — usage instructions (maps to API field {@code details.usage_instructions}).
 * Nullable, max 500.
 */
public record UsageInstructions(String value) {

  public UsageInstructions {
    if (value != null && value.length() > 500) {
      throw new IllegalArgumentException(
          "UsageInstructions cannot exceed 500 characters, got: " + value.length());
    }
  }

  public static UsageInstructions of(String value) {
    return new UsageInstructions(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
