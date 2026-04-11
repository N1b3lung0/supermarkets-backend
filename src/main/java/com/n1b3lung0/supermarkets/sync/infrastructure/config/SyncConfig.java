package com.n1b3lung0.supermarkets.sync.infrastructure.config;

import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryRepositoryPort;
import com.n1b3lung0.supermarkets.product.application.port.input.command.DeactivateProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.command.UpsertProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.sync.application.command.SyncSupermarketCatalogHandler;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import com.n1b3lung0.supermarkets.sync.application.port.output.LatestPricesRefreshPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.PartitionMaintenancePort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.LatestPricesRefreshJdbcAdapter;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.PartitionMaintenanceJdbcAdapter;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.SyncRunJpaAdapter;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.mapper.SyncRunPersistenceMapper;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.repository.SpringSyncRunRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

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
  public PartitionMaintenancePort partitionMaintenanceAdapter(JdbcTemplate jdbcTemplate) {
    return new PartitionMaintenanceJdbcAdapter(jdbcTemplate);
  }

  @Bean
  public LatestPricesRefreshPort latestPricesRefreshAdapter(JdbcClient jdbcClient) {
    return new LatestPricesRefreshJdbcAdapter(jdbcClient);
  }

  @Bean
  public SyncSupermarketCatalogUseCase syncSupermarketCatalogUseCase(
      List<CategoryScraperPort> categoryScrapers,
      List<ProductScraperPort> productScrapers,
      RegisterCategoryUseCase registerCategoryUseCase,
      UpsertProductUseCase upsertProductUseCase,
      DeactivateProductUseCase deactivateProductUseCase,
      CategoryRepositoryPort categoryRepositoryPort,
      ProductRepositoryPort productRepositoryPort,
      SyncRunJpaAdapter syncRunJpaAdapter,
      PartitionMaintenancePort partitionMaintenancePort,
      LatestPricesRefreshPort latestPricesRefreshPort,
      MeterRegistry meterRegistry) {
    return new SyncSupermarketCatalogHandler(
        categoryScrapers,
        productScrapers,
        registerCategoryUseCase,
        upsertProductUseCase,
        deactivateProductUseCase,
        categoryRepositoryPort,
        productRepositoryPort,
        syncRunJpaAdapter,
        partitionMaintenancePort,
        latestPricesRefreshPort,
        meterRegistry);
  }
}
