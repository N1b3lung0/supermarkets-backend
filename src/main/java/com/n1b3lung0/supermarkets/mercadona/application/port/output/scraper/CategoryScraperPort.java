package com.n1b3lung0.supermarkets.mercadona.application.port.output.scraper;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;

/**
 * Output port — fetches all category levels from Mercadona's API and maps them to {@link
 * RegisterCategoryCommand} objects ready for the application layer.
 */
public interface CategoryScraperPort {

  /**
   * Returns commands for all 3 levels of categories (TOP → SUBCATEGORY → LEAF). The list is ordered
   * parents-before-children so callers can persist them in order.
   */
  List<RegisterCategoryCommand> fetchCategories(SupermarketId supermarketId);
}
