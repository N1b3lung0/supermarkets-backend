package com.n1b3lung0.supermarkets.category.application.command;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryRepositoryPort;
import com.n1b3lung0.supermarkets.category.domain.exception.DuplicateCategoryException;
import com.n1b3lung0.supermarkets.category.domain.model.Category;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryLevel;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryName;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryOrder;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.Objects;

/** Registers a new Category after checking uniqueness per (externalId, supermarketId). */
public class RegisterCategoryHandler implements RegisterCategoryUseCase {

  private final CategoryRepositoryPort repository;

  public RegisterCategoryHandler(CategoryRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public CategoryId execute(RegisterCategoryCommand command) {
    Objects.requireNonNull(command, "command is required");

    var externalId = ExternalCategoryId.of(command.externalId());
    var supermarketId = SupermarketId.of(command.supermarketId());

    if (repository.existsByExternalIdAndSupermarketId(externalId, supermarketId)) {
      throw new DuplicateCategoryException(externalId, supermarketId);
    }

    var level = buildLevel(command);
    var category =
        Category.create(
            CategoryName.of(command.name()),
            externalId,
            supermarketId,
            level,
            CategoryOrder.of(command.order()));

    repository.save(category);
    return category.getId();
  }

  private CategoryLevel buildLevel(RegisterCategoryCommand command) {
    return switch (command.levelType().toUpperCase()) {
      case "TOP" -> new CategoryLevel.Top();
      case "SUB" ->
          new CategoryLevel.Sub(
              CategoryId.of(
                  Objects.requireNonNull(command.parentId(), "parentId required for SUB level")));
      case "LEAF" ->
          new CategoryLevel.Leaf(
              CategoryId.of(
                  Objects.requireNonNull(command.parentId(), "parentId required for LEAF level")));
      default -> throw new IllegalArgumentException("Unknown levelType: " + command.levelType());
    };
  }
}
