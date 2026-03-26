package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — product badge flags (maps to API field {@code badges}). Both default to {@code
 * false}.
 */
public record ProductBadges(boolean isWater, boolean requiresAgeCheck) {

  /** Factory — both flags false (most products have no special badges). */
  public static ProductBadges none() {
    return new ProductBadges(false, false);
  }

  public static ProductBadges of(boolean isWater, boolean requiresAgeCheck) {
    return new ProductBadges(isWater, requiresAgeCheck);
  }
}
