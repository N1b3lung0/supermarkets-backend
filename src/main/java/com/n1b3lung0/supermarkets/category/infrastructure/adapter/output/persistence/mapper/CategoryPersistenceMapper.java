package com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.dto.CategorySummaryView;
import com.n1b3lung0.supermarkets.category.domain.model.Category;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryLevel;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryName;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryOrder;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;

/** Manual mapper — no MapStruct since the level sealed hierarchy needs custom logic. */
public class CategoryPersistenceMapper {

  // -------------------------------------------------------------------------
  // Domain → Entity
  // -------------------------------------------------------------------------

  public CategoryEntity toEntity(Category category) {
    var entity = new CategoryEntity();
    entity.setId(category.getId().value());
    entity.setName(category.getName().value());
    entity.setExternalId(category.getExternalId().value());
    entity.setSupermarketId(category.getSupermarketId().value());
    entity.setLevelType(levelTypeString(category.getLevel()));
    entity.setParentId(category.getLevel().parentId().map(CategoryId::value).orElse(null));
    entity.setSortOrder(category.getOrder().value());
    return entity;
  }

  // -------------------------------------------------------------------------
  // Entity → Domain
  // -------------------------------------------------------------------------

  public Category toDomain(CategoryEntity entity) {
    var level = buildLevel(entity);
    return Category.reconstitute(
        CategoryId.of(entity.getId()),
        CategoryName.of(entity.getName()),
        ExternalCategoryId.of(entity.getExternalId()),
        SupermarketId.of(entity.getSupermarketId()),
        level,
        CategoryOrder.of(entity.getSortOrder()));
  }

  // -------------------------------------------------------------------------
  // Entity → Detail view
  // -------------------------------------------------------------------------

  public CategoryDetailView toDetailView(CategoryEntity entity) {
    return new CategoryDetailView(
        entity.getId(),
        entity.getName(),
        entity.getExternalId(),
        entity.getSupermarketId(),
        entity.getLevelType(),
        entity.getParentId(),
        entity.getSortOrder(),
        entity.getCreatedAt());
  }

  // -------------------------------------------------------------------------
  // Entity → Summary view
  // -------------------------------------------------------------------------

  public CategorySummaryView toSummaryView(CategoryEntity entity) {
    return new CategorySummaryView(
        entity.getId(),
        entity.getName(),
        entity.getExternalId(),
        entity.getLevelType(),
        entity.getSupermarketId());
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private String levelTypeString(CategoryLevel level) {
    return switch (level) {
      case CategoryLevel.Top ignored -> "TOP";
      case CategoryLevel.Sub ignored -> "SUB";
      case CategoryLevel.Leaf ignored -> "LEAF";
    };
  }

  private CategoryLevel buildLevel(CategoryEntity entity) {
    return switch (entity.getLevelType()) {
      case "TOP" -> new CategoryLevel.Top();
      case "SUB" -> new CategoryLevel.Sub(CategoryId.of(entity.getParentId()));
      case "LEAF" -> new CategoryLevel.Leaf(CategoryId.of(entity.getParentId()));
      default -> throw new IllegalStateException("Unknown level_type: " + entity.getLevelType());
    };
  }
}
