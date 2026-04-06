package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key class for {@link ProductPriceEntity}.
 *
 * <p>Required because the {@code product_prices} table is partitioned by {@code recorded_at}, so
 * the database primary key is {@code (id, recorded_at)}. JPA requires all partition key columns to
 * participate in the entity identifier.
 */
public class ProductPriceEntityId implements Serializable {

  private UUID id;
  private Instant recordedAt;

  public ProductPriceEntityId() {}

  public ProductPriceEntityId(UUID id, Instant recordedAt) {
    this.id = id;
    this.recordedAt = recordedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProductPriceEntityId that)) {
      return false;
    }
    return Objects.equals(id, that.id) && Objects.equals(recordedAt, that.recordedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, recordedAt);
  }
}
