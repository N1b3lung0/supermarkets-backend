package com.n1b3lung0.supermarkets.comparison.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identity Value Object for a ProductComparison. */
public record ComparisonId(UUID value) {

  public ComparisonId {
    Objects.requireNonNull(value, "ComparisonId value is required");
  }

  public static ComparisonId generate() {
    return new ComparisonId(UUID.randomUUID());
  }

  public static ComparisonId of(UUID value) {
    return new ComparisonId(value);
  }
}
