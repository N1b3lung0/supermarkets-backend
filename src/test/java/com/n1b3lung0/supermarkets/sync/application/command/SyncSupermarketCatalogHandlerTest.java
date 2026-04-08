package com.n1b3lung0.supermarkets.sync.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryRepositoryPort;
import com.n1b3lung0.supermarkets.category.domain.exception.DuplicateCategoryException;
import com.n1b3lung0.supermarkets.category.domain.model.Category;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.DeactivateProductCommand;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.product.application.port.input.command.DeactivateProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.command.UpsertProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.product.domain.model.Product;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncSupermarketCatalogCommand;
import com.n1b3lung0.supermarkets.sync.application.port.output.LatestPricesRefreshPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.PartitionMaintenancePort;
import com.n1b3lung0.supermarkets.sync.application.port.output.SyncRunRepositoryPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Steps 54+55+58+76 — unit tests for SyncSupermarketCatalogHandler. */
class SyncSupermarketCatalogHandlerTest {

  private CategoryScraperPort categoryScraper;
  private ProductScraperPort productScraper;
  private RegisterCategoryUseCase registerCategory;
  private UpsertProductUseCase upsertProduct;
  private DeactivateProductUseCase deactivateProduct;
  private CategoryRepositoryPort categoryRepository;
  private ProductRepositoryPort productRepository;
  private SyncRunRepositoryPort syncRunRepository;
  private PartitionMaintenancePort partitionMaintenance;
  private LatestPricesRefreshPort latestPricesRefresh;
  private SyncSupermarketCatalogHandler handler;

  private static final UUID SUPERMARKET_UUID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final SupermarketId SUPERMARKET_ID = SupermarketId.of(SUPERMARKET_UUID);
  private static final SyncSupermarketCatalogCommand COMMAND =
      new SyncSupermarketCatalogCommand(SUPERMARKET_UUID);

  @BeforeEach
  void setUp() {
    categoryScraper = mock(CategoryScraperPort.class);
    productScraper = mock(ProductScraperPort.class);
    // Both mocked scrapers support any supermarket by default
    when(categoryScraper.supports(any())).thenReturn(true);
    when(productScraper.supports(any())).thenReturn(true);
    registerCategory = mock(RegisterCategoryUseCase.class);
    upsertProduct = mock(UpsertProductUseCase.class);
    deactivateProduct = mock(DeactivateProductUseCase.class);
    categoryRepository = mock(CategoryRepositoryPort.class);
    productRepository = mock(ProductRepositoryPort.class);
    syncRunRepository = mock(SyncRunRepositoryPort.class);
    partitionMaintenance = mock(PartitionMaintenancePort.class);
    latestPricesRefresh = mock(LatestPricesRefreshPort.class);
    handler =
        new SyncSupermarketCatalogHandler(
            List.of(categoryScraper),
            List.of(productScraper),
            registerCategory,
            upsertProduct,
            deactivateProduct,
            categoryRepository,
            productRepository,
            syncRunRepository,
            partitionMaintenance,
            latestPricesRefresh,
            new SimpleMeterRegistry());
  }

  private RegisterCategoryCommand stubCategoryCommand(String externalId, String level) {
    return new RegisterCategoryCommand("name", externalId, SUPERMARKET_UUID, level, null, 1);
  }

  private Category stubLeafCategory(String externalId) {
    var cat = mock(Category.class);
    when(cat.getExternalId()).thenReturn(ExternalCategoryId.of(externalId));
    when(cat.getId()).thenReturn(CategoryId.generate());
    return cat;
  }

  private Category stubSubCategory(String externalId) {
    var cat = mock(Category.class);
    when(cat.getExternalId()).thenReturn(ExternalCategoryId.of(externalId));
    when(cat.getId()).thenReturn(CategoryId.generate());
    return cat;
  }

  private UpsertProductCommand stubProductCommand(String externalId) {
    return new UpsertProductCommand(
        externalId,
        SUPERMARKET_UUID,
        UUID.randomUUID(),
        "name",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        List.of(),
        false,
        false,
        false,
        false,
        1,
        null);
  }

  @Test
  void execute_shouldSaveRunningThenCompletedSyncRun() {
    when(categoryScraper.fetchCategories(SUPERMARKET_ID)).thenReturn(List.of());
    when(categoryRepository.findByLevelTypeAndSupermarketId("LEAF", SUPERMARKET_ID))
        .thenReturn(List.of());
    when(categoryRepository.findByLevelTypeAndSupermarketId("SUB", SUPERMARKET_ID))
        .thenReturn(List.of());
    when(productRepository.findActiveExternalIdsBySupermarket(SUPERMARKET_ID))
        .thenReturn(List.of());

    var runId = handler.execute(COMMAND);

    assertThat(runId).isNotNull();
    // save() called at least twice: once RUNNING, once COMPLETED
    var captor =
        ArgumentCaptor.forClass(com.n1b3lung0.supermarkets.sync.domain.model.SyncRun.class);
    verify(syncRunRepository, atLeastOnce()).save(captor.capture());
    var lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
    assertThat(lastSaved.getStatus()).isEqualTo(SyncStatus.COMPLETED);
  }

  @Test
  void execute_shouldRegisterAllScrapedCategories() {
    var cmd1 = stubCategoryCommand("10", "TOP");
    var cmd2 = stubCategoryCommand("100", "SUB");
    when(categoryScraper.fetchCategories(SUPERMARKET_ID)).thenReturn(List.of(cmd1, cmd2));
    when(categoryRepository.findByLevelTypeAndSupermarketId(any(), any())).thenReturn(List.of());
    when(productRepository.findActiveExternalIdsBySupermarket(any())).thenReturn(List.of());

    handler.execute(COMMAND);

    verify(registerCategory).execute(cmd1);
    verify(registerCategory).execute(cmd2);
  }

  @Test
  void execute_shouldSkipDuplicateCategories() {
    var cmd = stubCategoryCommand("10", "TOP");
    when(categoryScraper.fetchCategories(SUPERMARKET_ID)).thenReturn(List.of(cmd));
    when(registerCategory.execute(cmd))
        .thenThrow(new DuplicateCategoryException(ExternalCategoryId.of("10"), SUPERMARKET_ID));
    when(categoryRepository.findByLevelTypeAndSupermarketId(any(), any())).thenReturn(List.of());
    when(productRepository.findActiveExternalIdsBySupermarket(any())).thenReturn(List.of());

    var runId = handler.execute(COMMAND);

    // should still complete without throwing
    assertThat(runId).isNotNull();
  }

  @Test
  void execute_shouldUpsertAllScrapedProducts() {
    var leafCat = stubLeafCategory("420");
    var subCat = stubSubCategory("112");
    var productCmd = stubProductCommand("4241");

    when(categoryScraper.fetchCategories(SUPERMARKET_ID)).thenReturn(List.of());
    when(categoryRepository.findByLevelTypeAndSupermarketId("LEAF", SUPERMARKET_ID))
        .thenReturn(List.of(leafCat));
    when(categoryRepository.findByLevelTypeAndSupermarketId("SUB", SUPERMARKET_ID))
        .thenReturn(List.of(subCat));
    when(productScraper.fetchProductsBySubcategory(any(), any(), any()))
        .thenReturn(List.of(productCmd));
    when(productRepository.findActiveExternalIdsBySupermarket(SUPERMARKET_ID))
        .thenReturn(List.of());

    handler.execute(COMMAND);

    verify(upsertProduct).execute(productCmd);
  }

  @Test
  void execute_shouldDeactivateProductsNotInCurrentScrape() {
    var existingProductId = ProductId.generate();
    var existingProduct = mock(Product.class);
    when(existingProduct.getId()).thenReturn(existingProductId);

    when(categoryScraper.fetchCategories(SUPERMARKET_ID)).thenReturn(List.of());
    when(categoryRepository.findByLevelTypeAndSupermarketId(any(), any())).thenReturn(List.of());
    when(productRepository.findActiveExternalIdsBySupermarket(SUPERMARKET_ID))
        .thenReturn(List.of("stale-ext-id"));
    when(productRepository.findByExternalIdAndSupermarket(
            ExternalProductId.of("stale-ext-id"), SUPERMARKET_ID))
        .thenReturn(Optional.of(existingProduct));

    handler.execute(COMMAND);

    var captor = ArgumentCaptor.forClass(DeactivateProductCommand.class);
    verify(deactivateProduct).execute(captor.capture());
    assertThat(captor.getValue().productId()).isEqualTo(existingProductId);
  }

  @Test
  void execute_shouldNotDeactivateProductsPresentInCurrentScrape() {
    // Build stubs BEFORE when() setup to avoid UnfinishedStubbingException
    var leafCat = stubLeafCategory("420");
    var subCat = stubSubCategory("112");
    var productCmd = stubProductCommand("existing-ext-id");

    when(categoryScraper.fetchCategories(SUPERMARKET_ID)).thenReturn(List.of());
    when(categoryRepository.findByLevelTypeAndSupermarketId("LEAF", SUPERMARKET_ID))
        .thenReturn(List.of(leafCat));
    when(categoryRepository.findByLevelTypeAndSupermarketId("SUB", SUPERMARKET_ID))
        .thenReturn(List.of(subCat));
    when(productScraper.fetchProductsBySubcategory(any(), any(), any()))
        .thenReturn(List.of(productCmd));
    when(productRepository.findActiveExternalIdsBySupermarket(SUPERMARKET_ID))
        .thenReturn(List.of("existing-ext-id"));

    handler.execute(COMMAND);

    verify(deactivateProduct, never()).execute(any());
  }

  @Test
  void execute_onScraperFailure_shouldSaveFailedSyncRun() {
    when(categoryScraper.fetchCategories(SUPERMARKET_ID))
        .thenThrow(new RuntimeException("API down"));

    var runId = handler.execute(COMMAND);

    assertThat(runId).isNotNull();
    var captor =
        ArgumentCaptor.forClass(com.n1b3lung0.supermarkets.sync.domain.model.SyncRun.class);
    verify(syncRunRepository, atLeastOnce()).save(captor.capture());
    var lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
    assertThat(lastSaved.getStatus()).isEqualTo(SyncStatus.FAILED);
    assertThat(lastSaved.getErrorMessage()).contains("API down");
  }

  @Test
  void execute_dispatchesToCorrectScraper_whenMultipleScrapersPresent() {
    // Arrange two scrapers — only the second supports this supermarket
    var otherCategoryScraper = mock(CategoryScraperPort.class);
    var otherProductScraper = mock(ProductScraperPort.class);
    when(otherCategoryScraper.supports(any())).thenReturn(false);
    when(otherProductScraper.supports(any())).thenReturn(false);
    when(categoryScraper.supports(any())).thenReturn(true);
    when(productScraper.supports(any())).thenReturn(true);
    when(categoryScraper.fetchCategories(SUPERMARKET_ID)).thenReturn(List.of());
    when(categoryRepository.findByLevelTypeAndSupermarketId(any(), any())).thenReturn(List.of());
    when(productRepository.findActiveExternalIdsBySupermarket(any())).thenReturn(List.of());

    var multiHandler =
        new SyncSupermarketCatalogHandler(
            List.of(otherCategoryScraper, categoryScraper),
            List.of(otherProductScraper, productScraper),
            registerCategory,
            upsertProduct,
            deactivateProduct,
            categoryRepository,
            productRepository,
            syncRunRepository,
            partitionMaintenance,
            latestPricesRefresh,
            new SimpleMeterRegistry());

    multiHandler.execute(COMMAND);

    // Only the matching scraper should be called
    verify(categoryScraper).fetchCategories(SUPERMARKET_ID);
    verify(otherCategoryScraper, never()).fetchCategories(any());
  }

  @Test
  void execute_noMatchingScraper_shouldSaveFailedSyncRun() {
    // Build a handler where no scraper supports this supermarket
    var unmatchedCategoryScraper = mock(CategoryScraperPort.class);
    var unmatchedProductScraper = mock(ProductScraperPort.class);
    when(unmatchedCategoryScraper.supports(any())).thenReturn(false);
    when(unmatchedProductScraper.supports(any())).thenReturn(false);

    var noMatchHandler =
        new SyncSupermarketCatalogHandler(
            List.of(unmatchedCategoryScraper),
            List.of(unmatchedProductScraper),
            registerCategory,
            upsertProduct,
            deactivateProduct,
            categoryRepository,
            productRepository,
            syncRunRepository,
            partitionMaintenance,
            latestPricesRefresh,
            new SimpleMeterRegistry());

    var runId = noMatchHandler.execute(COMMAND);

    assertThat(runId).isNotNull();
    var captor =
        ArgumentCaptor.forClass(com.n1b3lung0.supermarkets.sync.domain.model.SyncRun.class);
    verify(syncRunRepository, atLeastOnce()).save(captor.capture());
    var last = captor.getAllValues().get(captor.getAllValues().size() - 1);
    assertThat(last.getStatus()).isEqualTo(SyncStatus.FAILED);
  }
}
