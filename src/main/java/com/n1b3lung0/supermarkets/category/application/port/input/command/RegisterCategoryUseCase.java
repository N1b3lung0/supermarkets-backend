package com.n1b3lung0.supermarkets.category.application.port.input.command;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;

/** Use case — register a new Category. */
public interface RegisterCategoryUseCase {
  CategoryId execute(RegisterCategoryCommand command);
}
