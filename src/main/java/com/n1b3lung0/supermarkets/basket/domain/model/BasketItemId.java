package com.n1b3lung0.supermarkets.basket.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identity Value Object for a BasketItem entity. */
public record BasketItemId(UUID value) {

  public BasketItemId {
    Objects.requireNonNull(value, "BasketItemId value is required");
  }

  public static BasketItemId generate() {
    return new BasketItemId(UUID.randomUUID());
  }

  public static BasketItemId of(UUID value) {
    return new BasketItemId(value);
  }
}
