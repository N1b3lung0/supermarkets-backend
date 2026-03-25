package com.n1b3lung0.supermarkets.supermarket.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Value Object representing the unique identity of a Supermarket. */
public record SupermarketId(UUID value) {

  public SupermarketId {
    Objects.requireNonNull(value, "SupermarketId value is required");
  }

  public static SupermarketId generate() {
    return new SupermarketId(UUID.randomUUID());
  }

  public static SupermarketId of(UUID value) {
    return new SupermarketId(value);
  }

  public static SupermarketId of(String value) {
    return new SupermarketId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
