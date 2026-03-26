package com.n1b3lung0.supermarkets.category.domain.model;

import java.util.UUID;

/** Value Object — internal UUID identifier for a Category. */
public record CategoryId(UUID value) {

  public CategoryId {
    java.util.Objects.requireNonNull(value, "CategoryId value is required");
  }

  public static CategoryId of(UUID value) {
    return new CategoryId(value);
  }

  public static CategoryId generate() {
    return new CategoryId(UUID.randomUUID());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
