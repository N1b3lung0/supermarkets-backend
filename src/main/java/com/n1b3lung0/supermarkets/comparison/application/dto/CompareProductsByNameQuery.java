package com.n1b3lung0.supermarkets.comparison.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Query object for the compare-products-by-name use case. {@code supermarketIds} may be empty — in
 * that case all supermarkets are searched.
 */
public record CompareProductsByNameQuery(String searchTerm, List<UUID> supermarketIds) {

  public CompareProductsByNameQuery {
    if (searchTerm == null || searchTerm.isBlank()) {
      throw new IllegalArgumentException("searchTerm must not be blank");
    }
    supermarketIds = supermarketIds == null ? List.of() : List.copyOf(supermarketIds);
  }
}
