package com.n1b3lung0.supermarkets.product.application.query;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.ListProductsByCategoryQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsByCategoryUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductQueryPort;
import java.util.Objects;
import org.springframework.data.domain.Page;

/** Returns a paginated list of products belonging to a given category. */
public class ListProductsByCategoryHandler implements ListProductsByCategoryUseCase {

  private final ProductQueryPort queryPort;

  public ListProductsByCategoryHandler(ProductQueryPort queryPort) {
    this.queryPort = queryPort;
  }

  @Override
  public Page<ProductSummaryView> execute(ListProductsByCategoryQuery query) {
    Objects.requireNonNull(query, "query is required");
    return queryPort.findSummariesByCategory(CategoryId.of(query.categoryId()), query.pageable());
  }
}
