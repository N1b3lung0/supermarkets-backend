package com.n1b3lung0.supermarkets.category.application.port.input.query;

import com.n1b3lung0.supermarkets.category.application.dto.CategorySummaryView;
import com.n1b3lung0.supermarkets.category.application.dto.ListCategoriesQuery;
import org.springframework.data.domain.Page;

/** Use case — list Categories with optional filters. */
public interface ListCategoriesUseCase {
  Page<CategorySummaryView> execute(ListCategoriesQuery query);
}
