package com.n1b3lung0.supermarkets.category.domain.model;

import java.util.Objects;

/** Value Object — display name of a Category (max 255 chars). */
public record CategoryName(String value) {

  public CategoryName {
    Objects.requireNonNull(value, "CategoryName value is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException("CategoryName cannot be blank");
    }
    if (value.length() > 255) {
      throw new IllegalArgumentException(
          "CategoryName cannot exceed 255 characters, got: " + value.length());
    }
  }

  public static CategoryName of(String value) {
    return new CategoryName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
