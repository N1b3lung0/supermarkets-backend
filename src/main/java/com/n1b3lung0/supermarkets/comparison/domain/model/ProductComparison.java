package com.n1b3lung0.supermarkets.comparison.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Value Object representing the result of a price comparison search across supermarkets. Immutable
 * — all derived views are computed on-the-fly from the raw {@code matches} list.
 */
public record ProductComparison(ComparisonId id, String searchTerm, List<ProductMatch> matches) {

  public ProductComparison {
    Objects.requireNonNull(id, "id is required");
    Objects.requireNonNull(searchTerm, "searchTerm is required");
    Objects.requireNonNull(matches, "matches is required");
    if (searchTerm.isBlank()) {
      throw new IllegalArgumentException("searchTerm must not be blank");
    }
    matches = List.copyOf(matches); // defensive copy — makes the list unmodifiable
  }

  /** Factory — generates a fresh ComparisonId. */
  public static ProductComparison of(String searchTerm, List<ProductMatch> matches) {
    return new ProductComparison(ComparisonId.generate(), searchTerm, matches);
  }

  /**
   * Returns the {@link ProductMatch} with the lowest unit price. Returns empty if the match list is
   * empty. When multiple matches share the same lowest price the first one encountered is returned.
   */
  public Optional<ProductMatch> cheapest() {
    return matches.stream().min(Comparator.comparing(ProductMatch::unitPriceAmount));
  }

  /** Returns all matches sorted by unit price ascending. */
  public List<ProductMatch> sortedByPrice() {
    return matches.stream().sorted(Comparator.comparing(ProductMatch::unitPriceAmount)).toList();
  }
}
