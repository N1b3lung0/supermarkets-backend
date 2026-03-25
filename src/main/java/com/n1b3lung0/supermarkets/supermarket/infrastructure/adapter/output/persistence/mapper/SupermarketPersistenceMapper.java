package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketSummaryView;
import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketCountry;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.entity.SupermarketEntity;

/** Maps between the Supermarket domain model and its JPA entity / read projections. */
public class SupermarketPersistenceMapper {

  public SupermarketEntity toEntity(Supermarket supermarket) {
    return new SupermarketEntity(
        supermarket.getId().value(),
        supermarket.getName().value(),
        supermarket.getCountry().value());
  }

  public Supermarket toDomain(SupermarketEntity entity) {
    // Uses reconstitute() — no events, no invariant re-checks
    return Supermarket.reconstitute(
        SupermarketId.of(entity.getId()),
        SupermarketName.of(entity.getName()),
        SupermarketCountry.of(entity.getCountry()));
  }

  public SupermarketDetailView toDetailView(SupermarketEntity entity) {
    return new SupermarketDetailView(
        entity.getId(), entity.getName(), entity.getCountry(), entity.getCreatedAt());
  }

  public SupermarketSummaryView toSummaryView(SupermarketEntity entity) {
    return new SupermarketSummaryView(entity.getId(), entity.getName(), entity.getCountry());
  }
}
