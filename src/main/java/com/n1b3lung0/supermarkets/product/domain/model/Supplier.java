package com.n1b3lung0.supermarkets.product.domain.model;

import java.util.Objects;

/**
 * Value Object — a single product supplier entry (maps to API field {@code
 * details.suppliers[].name}).
 */
public record Supplier(String name) {

  public Supplier {
    Objects.requireNonNull(name, "Supplier name is required");
    if (name.isBlank()) {
      throw new IllegalArgumentException("Supplier name cannot be blank");
    }
    if (name.length() > 255) {
      throw new IllegalArgumentException(
          "Supplier name cannot exceed 255 characters, got: " + name.length());
    }
  }

  public static Supplier of(String name) {
    return new Supplier(name);
  }
}
