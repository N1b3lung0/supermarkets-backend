package com.n1b3lung0.supermarkets.comparison.application.query;

import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductComparisonView;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductMatchView;
import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.comparison.application.port.output.ProductComparisonQueryPort;
import com.n1b3lung0.supermarkets.comparison.domain.model.ProductComparison;
import com.n1b3lung0.supermarkets.comparison.domain.model.ProductMatch;
import java.util.Objects;

/**
 * Handles a price comparison query by name. The domain model ({@link ProductComparison}) provides
 * sorting and cheapest-product logic; this handler maps the result to the view DTO.
 */
public class CompareProductsByNameHandler implements CompareProductsByNameUseCase {

  private final ProductComparisonQueryPort queryPort;

  public CompareProductsByNameHandler(ProductComparisonQueryPort queryPort) {
    this.queryPort = queryPort;
  }

  @Override
  public ProductComparisonView execute(CompareProductsByNameQuery query) {
    Objects.requireNonNull(query, "query is required");

    var rawMatches = queryPort.findMatchesByName(query.searchTerm(), query.supermarketIds());
    var comparison = ProductComparison.of(query.searchTerm(), rawMatches);

    var sortedViews = comparison.sortedByPrice().stream().map(this::toView).toList();

    var cheapest = comparison.cheapest();
    var cheapestSupermarketId = cheapest.map(ProductMatch::supermarketId).orElse(null);
    var cheapestSupermarketName = cheapest.map(ProductMatch::supermarketName).orElse(null);

    return new ProductComparisonView(
        query.searchTerm(), sortedViews, cheapestSupermarketId, cheapestSupermarketName);
  }

  private ProductMatchView toView(ProductMatch m) {
    return new ProductMatchView(
        m.productId(),
        m.supermarketId(),
        m.supermarketName(),
        m.productName(),
        m.unitPrice().amount(),
        m.bulkPrice() != null ? m.bulkPrice().amount() : null,
        m.referencePrice() != null ? m.referencePrice().amount() : null,
        m.referenceFormat(),
        m.priceRecordedAt());
  }
}
