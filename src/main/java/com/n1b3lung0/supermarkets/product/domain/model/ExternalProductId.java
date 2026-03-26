package com.n1b3lung0.supermarkets.product.domain.model;

import java.util.Objects;

/**
 * Value Object — the supermarket's own product identifier (e.g. "3400" for Mercadona). Stored as
 * String to accommodate different formats across supermarket chains.
 */
public record ExternalProductId(String value) {

  public ExternalProductId {
    Objects.requireNonNull(value, "ExternalProductId value is required");
    if (value.isBlank()) {
      throw new IllegalArgumentException("ExternalProductId cannot be blank");
    }
  }

  public static ExternalProductId of(String value) {
    return new ExternalProductId(value);
  }

  /** Convenience factory for integer-based external ids (e.g. Mercadona). */
  public static ExternalProductId of(int value) {
    return new ExternalProductId(String.valueOf(value));
  }

  @Override
  public String toString() {
    return value;
  }
}
