package com.n1b3lung0.supermarkets.category.application.port.output;

import com.n1b3lung0.supermarkets.category.domain.model.Category;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.Optional;

/** Write-side port — persistence operations for Category. */
public interface CategoryRepositoryPort {

  void save(Category category);

  Optional<Category> findById(CategoryId id);

  boolean existsByExternalIdAndSupermarketId(
      ExternalCategoryId externalId, SupermarketId supermarketId);
}
