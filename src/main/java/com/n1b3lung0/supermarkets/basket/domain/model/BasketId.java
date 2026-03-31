package com.n1b3lung0.supermarkets.basket.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identity Value Object for a Basket aggregate. */
public record BasketId(UUID value) {

  public BasketId {
    Objects.requireNonNull(value, "BasketId value is required");
  }

  public static BasketId generate() {
    return new BasketId(UUID.randomUUID());
  }

  public static BasketId of(UUID value) {
    return new BasketId(value);
  }
}
