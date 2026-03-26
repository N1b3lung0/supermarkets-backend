package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — ingredient list (maps to API field {@code nutrition_information.ingredients}).
 * Nullable, max 2000.
 */
public record Ingredients(String value) {

  public Ingredients {
    if (value != null && value.length() > 2000) {
      throw new IllegalArgumentException(
          "Ingredients cannot exceed 2000 characters, got: " + value.length());
    }
  }

  public static Ingredients of(String value) {
    return new Ingredients(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
