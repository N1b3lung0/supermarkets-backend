package com.n1b3lung0.supermarkets.category.application.port.output;

import com.n1b3lung0.supermarkets.category.domain.model.Category;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;
import java.util.Optional;

/** Write-side port — persistence operations for Category. */
public interface CategoryRepositoryPort {

  void save(Category category);

  Optional<Category> findById(CategoryId id);

  Optional<Category> findByExternalIdAndSupermarketId(
      ExternalCategoryId externalId, SupermarketId supermarketId);

  boolean existsByExternalIdAndSupermarketId(
      ExternalCategoryId externalId, SupermarketId supermarketId);

  /**
   * Returns all categories of a given level type for a supermarket. Used by the sync handler to
   * build the leaf-category index and collect SUB external ids.
   */
  List<Category> findByLevelTypeAndSupermarketId(String levelType, SupermarketId supermarketId);
}
