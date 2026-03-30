package com.n1b3lung0.supermarkets.comparison.application.port.output;

import com.n1b3lung0.supermarkets.comparison.domain.model.ProductMatch;
import java.util.List;
import java.util.UUID;

/**
 * Output port for the comparison query. Implementations live in the infrastructure layer and query
 * the database directly via SQL (no domain entities involved).
 */
public interface ProductComparisonQueryPort {

  /**
   * Finds active products whose name contains {@code searchTerm} (case-insensitive) and returns the
   * latest known price for each. When {@code supermarketIds} is empty, all supermarkets are
   * included.
   */
  List<ProductMatch> findMatchesByName(String searchTerm, List<UUID> supermarketIds);
}
