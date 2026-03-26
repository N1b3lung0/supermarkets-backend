package com.n1b3lung0.supermarkets.category.domain.model;

/** Value Object — display order within the same level (≥ 0). */
public record CategoryOrder(int value) {

  public CategoryOrder {
    if (value < 0) {
      throw new IllegalArgumentException("CategoryOrder must be >= 0, got: " + value);
    }
  }

  public static CategoryOrder of(int value) {
    return new CategoryOrder(value);
  }

  public static CategoryOrder zero() {
    return new CategoryOrder(0);
  }
}
