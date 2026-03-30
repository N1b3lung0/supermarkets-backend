package com.n1b3lung0.supermarkets.sync.infrastructure.config;

import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryRepositoryPort;
import com.n1b3lung0.supermarkets.product.application.port.input.command.DeactivateProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.command.UpsertProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.sync.application.command.SyncSupermarketCatalogHandler;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.SyncRunJpaAdapter;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.mapper.SyncRunPersistenceMapper;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.repository.SpringSyncRunRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires together all Sync use cases, handlers, adapters and mappers. Domain and application classes
 * have zero Spring annotations — all wiring happens here.
 */
@Configuration
public class SyncConfig {

  @Bean
  public SyncRunPersistenceMapper syncRunPersistenceMapper() {
    return new SyncRunPersistenceMapper();
  }

  @Bean
  public SyncRunJpaAdapter syncRunJpaAdapter(
      SpringSyncRunRepository repository, SyncRunPersistenceMapper mapper) {
    return new SyncRunJpaAdapter(repository, mapper);
  }

  @Bean
  public SyncSupermarketCatalogUseCase syncSupermarketCatalogUseCase(
      CategoryScraperPort categoryScraper,
      ProductScraperPort productScraper,
      RegisterCategoryUseCase registerCategoryUseCase,
      UpsertProductUseCase upsertProductUseCase,
      DeactivateProductUseCase deactivateProductUseCase,
      CategoryRepositoryPort categoryRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      SyncRunJpaAdapter syncRunJpaAdapter) {
    return new SyncSupermarketCatalogHandler(
        categoryScraper,
        productScraper,
        registerCategoryUseCase,
        upsertProductUseCase,
        deactivateProductUseCase,
        categoryRepositoryPort,
        productRepositoryPort,
        syncRunJpaAdapter);
  }
}
