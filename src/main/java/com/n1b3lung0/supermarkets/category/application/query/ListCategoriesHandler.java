package com.n1b3lung0.supermarkets.category.application.query;

import com.n1b3lung0.supermarkets.category.application.dto.CategorySummaryView;
import com.n1b3lung0.supermarkets.category.application.dto.ListCategoriesQuery;
import com.n1b3lung0.supermarkets.category.application.port.input.query.ListCategoriesUseCase;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryQueryPort;
import java.util.Objects;
import org.springframework.data.domain.Page;

/** Returns a paginated list of Category summaries with optional supermarket / level filters. */
public class ListCategoriesHandler implements ListCategoriesUseCase {

  private final CategoryQueryPort queryPort;

  public ListCategoriesHandler(CategoryQueryPort queryPort) {
    this.queryPort = queryPort;
  }

  @Override
  public Page<CategorySummaryView> execute(ListCategoriesQuery query) {
    Objects.requireNonNull(query, "query is required");
    return queryPort.findAll(query.supermarketId(), query.levelType(), query.pageable());
  }
}
