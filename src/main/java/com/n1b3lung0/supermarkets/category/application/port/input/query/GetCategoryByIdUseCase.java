package com.n1b3lung0.supermarkets.category.application.port.input.query;

import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.dto.GetCategoryByIdQuery;

/** Use case — get a single Category by id. */
public interface GetCategoryByIdUseCase {
  CategoryDetailView execute(GetCategoryByIdQuery query);
}
