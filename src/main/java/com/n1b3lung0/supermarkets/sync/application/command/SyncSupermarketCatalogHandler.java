package com.n1b3lung0.supermarkets.sync.application.command;

import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryRepositoryPort;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.DeactivateProductCommand;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.product.application.port.input.command.DeactivateProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.command.UpsertProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncSupermarketCatalogCommand;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import com.n1b3lung0.supermarkets.sync.application.port.output.SyncRunRepositoryPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRun;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates a full catalog sync for one supermarket:
 *
 * <ol>
 *   <li>Fetches all categories (TOP → SUB → LEAF) and upserts them.
 *   <li>Fetches all products per level-1 subcategory and upserts them (price always recorded).
 *   <li>Deactivates products that disappeared from the current scrape.
 *   <li>Records the result in a {@link SyncRun}.
 * </ol>
 */
public class SyncSupermarketCatalogHandler implements SyncSupermarketCatalogUseCase {

  private static final Logger log = LoggerFactory.getLogger(SyncSupermarketCatalogHandler.class);

  private final List<CategoryScraperPort> categoryScrapers;
  private final List<ProductScraperPort> productScrapers;
  private final RegisterCategoryUseCase registerCategory;
  private final UpsertProductUseCase upsertProduct;
  private final DeactivateProductUseCase deactivateProduct;
  private final CategoryRepositoryPort categoryRepository;
  private final ProductRepositoryPort productRepository;
  private final SyncRunRepositoryPort syncRunRepository;

  public SyncSupermarketCatalogHandler(
      List<CategoryScraperPort> categoryScrapers,
      List<ProductScraperPort> productScrapers,
      RegisterCategoryUseCase registerCategory,
      UpsertProductUseCase upsertProduct,
      DeactivateProductUseCase deactivateProduct,
      CategoryRepositoryPort categoryRepository,
      ProductRepositoryPort productRepository,
      SyncRunRepositoryPort syncRunRepository) {
    this.categoryScrapers = List.copyOf(categoryScrapers);
    this.productScrapers = List.copyOf(productScrapers);
    this.registerCategory = registerCategory;
    this.upsertProduct = upsertProduct;
    this.deactivateProduct = deactivateProduct;
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
    this.syncRunRepository = syncRunRepository;
  }

  private CategoryScraperPort findCategoryScraper(SupermarketId supermarketId) {
    return categoryScrapers.stream()
        .filter(s -> s.supports(supermarketId))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No CategoryScraperPort available for supermarket " + supermarketId.value()));
  }

  private ProductScraperPort findProductScraper(SupermarketId supermarketId) {
    return productScrapers.stream()
        .filter(s -> s.supports(supermarketId))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No ProductScraperPort available for supermarket " + supermarketId.value()));
  }

  @Override
  public SyncRunId execute(SyncSupermarketCatalogCommand command) {
    Objects.requireNonNull(command, "command is required");
    var supermarketId = SupermarketId.of(command.supermarketId());
    var syncRun = SyncRun.start(supermarketId);
    syncRunRepository.save(syncRun);

    try {
      // ------------------------------------------------------------------
      // 1. Sync categories
      // ------------------------------------------------------------------
      var categoryScraper = findCategoryScraper(supermarketId);
      var categoryCommands = categoryScraper.fetchCategories(supermarketId);
      log.info(
          "[Sync] Fetched {} category commands for supermarket {}",
          categoryCommands.size(),
          supermarketId.value());

      int categoriesSynced = 0;
      for (var cmd : categoryCommands) {
        try {
          registerCategory.execute(cmd);
          categoriesSynced++;
        } catch (
            com.n1b3lung0.supermarkets.category.domain.exception.DuplicateCategoryException e) {
          // Already exists — idempotent, skip
          log.debug("[Sync] Category externalId={} already exists, skipping", cmd.externalId());
        }
      }
      log.info("[Sync] Categories synced: {}", categoriesSynced);

      // ------------------------------------------------------------------
      // 2. Build leafCategoryIndex: ExternalCategoryId → CategoryId
      // ------------------------------------------------------------------
      var leafCategories =
          categoryRepository.findByLevelTypeAndSupermarketId("LEAF", supermarketId);
      var leafCategoryIndex = new HashMap<ExternalCategoryId, CategoryId>();
      for (var leaf : leafCategories) {
        leafCategoryIndex.put(leaf.getExternalId(), leaf.getId());
      }
      log.info("[Sync] Built leaf index with {} entries", leafCategoryIndex.size());

      // ------------------------------------------------------------------
      // 3. Sync products per SUB category
      // ------------------------------------------------------------------
      var subCategories = categoryRepository.findByLevelTypeAndSupermarketId("SUB", supermarketId);
      log.info("[Sync] Found {} SUB categories to iterate for products", subCategories.size());

      var productScraper = findProductScraper(supermarketId);
      List<UpsertProductCommand> allProductCommands = new ArrayList<>();
      for (var sub : subCategories) {
        var cmds =
            productScraper.fetchProductsBySubcategory(
                supermarketId, sub.getExternalId(), leafCategoryIndex);
        allProductCommands.addAll(cmds);
      }
      log.info("[Sync] Fetched {} product commands", allProductCommands.size());

      Set<String> scrapedExternalIds = new HashSet<>();
      int productsSynced = 0;
      for (var cmd : allProductCommands) {
        upsertProduct.execute(cmd);
        scrapedExternalIds.add(cmd.externalId());
        productsSynced++;
      }

      // ------------------------------------------------------------------
      // 4. Deactivate products not in current scrape
      // ------------------------------------------------------------------
      var activeExternalIds = productRepository.findActiveExternalIdsBySupermarket(supermarketId);
      int productsDeactivated = 0;
      for (var extId : activeExternalIds) {
        if (!scrapedExternalIds.contains(extId)) {
          var product =
              productRepository.findByExternalIdAndSupermarket(
                  ExternalProductId.of(extId), supermarketId);
          if (product.isPresent()) {
            deactivateProduct.execute(new DeactivateProductCommand(product.get().getId()));
            productsDeactivated++;
          }
        }
      }
      log.info("[Sync] Products synced: {}, deactivated: {}", productsSynced, productsDeactivated);

      // ------------------------------------------------------------------
      // 5. Complete the sync run
      // ------------------------------------------------------------------
      syncRun.complete(categoriesSynced, productsSynced, productsDeactivated);
      syncRunRepository.save(syncRun);

      log.info(
          "[Sync] Completed for supermarket {}: categories={}, products={}, deactivated={}",
          supermarketId.value(),
          categoriesSynced,
          productsSynced,
          productsDeactivated);

    } catch (Exception ex) {
      log.error("[Sync] Failed for supermarket {}: {}", supermarketId.value(), ex.getMessage(), ex);
      syncRun.fail(ex.getMessage());
      syncRunRepository.save(syncRun);
    }

    return syncRun.getId();
  }
}
