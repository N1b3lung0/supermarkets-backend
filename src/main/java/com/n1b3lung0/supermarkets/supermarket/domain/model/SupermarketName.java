package com.n1b3lung0.supermarkets.supermarket.domain.model;

import java.util.Objects;

/** Value Object representing the commercial name of a Supermarket (e.g. "Mercadona"). */
public record SupermarketName(String value) {

  public SupermarketName {
    Objects.requireNonNull(value, "SupermarketName value is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException("SupermarketName cannot be blank");
    }
    if (value.length() > 100) {
      throw new IllegalArgumentException(
          "SupermarketName cannot exceed 100 characters, got: " + value.length());
    }
  }

  public static SupermarketName of(String value) {
    return new SupermarketName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
