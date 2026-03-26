package com.n1b3lung0.supermarkets.category.domain.model;

import java.util.Objects;

/**
 * Value Object — the Mercadona integer category id transported as a String (e.g. "12", "112",
 * "420"). Using String avoids int→long widening surprises and keeps the VO supplier-agnostic for
 * future scrapers.
 */
public record ExternalCategoryId(String value) {

  public ExternalCategoryId {
    Objects.requireNonNull(value, "ExternalCategoryId value is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException("ExternalCategoryId cannot be blank");
    }
  }

  public static ExternalCategoryId of(String value) {
    return new ExternalCategoryId(value);
  }

  public static ExternalCategoryId of(int value) {
    return new ExternalCategoryId(String.valueOf(value));
  }

  @Override
  public String toString() {
    return value;
  }
}
