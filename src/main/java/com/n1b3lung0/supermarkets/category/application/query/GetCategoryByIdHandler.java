package com.n1b3lung0.supermarkets.category.application.query;

import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.dto.GetCategoryByIdQuery;
import com.n1b3lung0.supermarkets.category.application.port.input.query.GetCategoryByIdUseCase;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryQueryPort;
import com.n1b3lung0.supermarkets.category.domain.exception.CategoryNotFoundException;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import java.util.Objects;

/** Returns the full detail view of a Category or throws {@link CategoryNotFoundException}. */
public class GetCategoryByIdHandler implements GetCategoryByIdUseCase {

  private final CategoryQueryPort queryPort;

  public GetCategoryByIdHandler(CategoryQueryPort queryPort) {
    this.queryPort = queryPort;
  }

  @Override
  public CategoryDetailView execute(GetCategoryByIdQuery query) {
    Objects.requireNonNull(query, "query is required");
    var id = CategoryId.of(query.id());
    return queryPort.findDetailById(id).orElseThrow(() -> new CategoryNotFoundException(id));
  }
}
