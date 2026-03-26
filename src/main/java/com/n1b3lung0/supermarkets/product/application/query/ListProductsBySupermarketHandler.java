package com.n1b3lung0.supermarkets.product.application.query;

import com.n1b3lung0.supermarkets.product.application.dto.ListProductsBySupermarketQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsBySupermarketUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductQueryPort;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.Objects;
import org.springframework.data.domain.Page;

/** Returns a paginated list of products belonging to a given supermarket. */
public class ListProductsBySupermarketHandler implements ListProductsBySupermarketUseCase {

  private final ProductQueryPort queryPort;

  public ListProductsBySupermarketHandler(ProductQueryPort queryPort) {
    this.queryPort = queryPort;
  }

  @Override
  public Page<ProductSummaryView> execute(ListProductsBySupermarketQuery query) {
    Objects.requireNonNull(query, "query is required");
    return queryPort.findSummariesBySupermarket(
        SupermarketId.of(query.supermarketId()), query.pageable());
  }
}
