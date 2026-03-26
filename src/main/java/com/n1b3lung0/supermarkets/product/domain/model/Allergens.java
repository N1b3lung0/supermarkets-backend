package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — allergen information (maps to API field {@code nutrition_information.allergens}).
 * May contain HTML markup from the Mercadona API. Nullable, max 2000.
 */
public record Allergens(String value) {

  public Allergens {
    if (value != null && value.length() > 2000) {
      throw new IllegalArgumentException(
          "Allergens cannot exceed 2000 characters, got: " + value.length());
    }
  }

  public static Allergens of(String value) {
    return new Allergens(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
