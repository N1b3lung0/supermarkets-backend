package com.n1b3lung0.supermarkets.sync.application.port.output.scraper;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;
import java.util.Map;

/**
 * Output port — fetches all products for a given level-1 subcategory from a supermarket's API and
 * maps them to {@link UpsertProductCommand} objects ready for the application layer.
 */
public interface ProductScraperPort {

  /**
   * Calls the supermarket API for the given level-1 subcategory and returns one {@link
   * UpsertProductCommand} per product found across all leaf groups.
   *
   * @param supermarketId the supermarket this sync belongs to
   * @param level1ExternalId the external id of the level-1 subcategory
   * @param leafCategoryIndex maps each leaf-group external id → its internal {@link CategoryId};
   *     built by the sync handler after categories are persisted
   */
  List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId level1ExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex);
}
