package com.n1b3lung0.supermarkets.supermarket.domain;

import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketCountry;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import java.util.UUID;

/** ObjectMother for Supermarket test fixtures. */
public final class SupermarketMother {

  private SupermarketMother() {}

  public static Supermarket mercadona() {
    return Supermarket.reconstitute(
        SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        SupermarketName.of("Mercadona"),
        SupermarketCountry.of("ES"));
  }

  public static Supermarket carrefour() {
    return Supermarket.reconstitute(
        SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000002")),
        SupermarketName.of("Carrefour"),
        SupermarketCountry.of("ES"));
  }

  public static Supermarket anySpanish() {
    return Supermarket.create(SupermarketName.of("TestMarket"), SupermarketCountry.of("ES"));
  }

  public static Supermarket withName(String name) {
    return Supermarket.create(SupermarketName.of(name), SupermarketCountry.of("ES"));
  }
}
