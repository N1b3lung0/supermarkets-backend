package com.n1b3lung0.supermarkets.product.infrastructure.config;

import com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.cache.CachingCompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.product.application.command.DeactivateProductHandler;
import com.n1b3lung0.supermarkets.product.application.command.RecordProductPriceHandler;
import com.n1b3lung0.supermarkets.product.application.command.UpsertProductHandler;
import com.n1b3lung0.supermarkets.product.application.port.input.command.DeactivateProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.command.RecordProductPriceUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.command.UpsertProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductPriceHistoryUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsByCategoryUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsBySupermarketUseCase;
import com.n1b3lung0.supermarkets.product.application.query.GetProductByIdHandler;
import com.n1b3lung0.supermarkets.product.application.query.GetProductPriceHistoryHandler;
import com.n1b3lung0.supermarkets.product.application.query.ListProductsByCategoryHandler;
import com.n1b3lung0.supermarkets.product.application.query.ListProductsBySupermarketHandler;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.cache.CachingGetProductByIdUseCase;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.input.event.ProductCacheEvictionListener;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.metrics.MeteredRecordProductPriceUseCase;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.ProductJpaAdapter;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.ProductPriceJpaAdapter;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper.ProductPersistenceMapper;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper.ProductPricePersistenceMapper;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository.SpringProductPriceRepository;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository.SpringProductRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires together all Product use cases, handlers, adapters, and mappers. Domain and application
 * classes have zero Spring annotations — all wiring happens here.
 */
@Configuration
public class ProductConfig {

  @Bean
  public ProductPersistenceMapper productPersistenceMapper() {
    return new ProductPersistenceMapper();
  }

  @Bean
  public ProductPricePersistenceMapper productPricePersistenceMapper() {
    return new ProductPricePersistenceMapper();
  }

  @Bean
  public ProductJpaAdapter productJpaAdapter(
      SpringProductRepository productRepository,
      SpringProductPriceRepository priceRepository,
      ProductPersistenceMapper mapper,
      ProductPricePersistenceMapper priceMapper) {
    return new ProductJpaAdapter(productRepository, priceRepository, mapper, priceMapper);
  }

  @Bean
  public ProductPriceJpaAdapter productPriceJpaAdapter(
      SpringProductPriceRepository priceRepository, ProductPricePersistenceMapper priceMapper) {
    return new ProductPriceJpaAdapter(priceRepository, priceMapper);
  }

  @Bean
  public RecordProductPriceUseCase recordProductPriceUseCase(
      ProductPriceJpaAdapter priceAdapter, MeterRegistry meterRegistry) {
    return new MeteredRecordProductPriceUseCase(
        new RecordProductPriceHandler(priceAdapter), meterRegistry);
  }

  @Bean
  public UpsertProductUseCase upsertProductUseCase(
      ProductJpaAdapter productAdapter, RecordProductPriceUseCase recordPrice) {
    return new UpsertProductHandler(productAdapter, recordPrice);
  }

  @Bean
  public DeactivateProductUseCase deactivateProductUseCase(ProductJpaAdapter productAdapter) {
    return new DeactivateProductHandler(productAdapter);
  }

  @Bean
  public CachingGetProductByIdUseCase getProductByIdUseCase(
      ProductJpaAdapter productAdapter, CacheManager cacheManager) {
    return new CachingGetProductByIdUseCase(
        new GetProductByIdHandler(productAdapter), cacheManager);
  }

  @Bean
  public ListProductsByCategoryUseCase listProductsByCategoryUseCase(
      ProductJpaAdapter productAdapter) {
    return new ListProductsByCategoryHandler(productAdapter);
  }

  @Bean
  public ListProductsBySupermarketUseCase listProductsBySupermarketUseCase(
      ProductJpaAdapter productAdapter) {
    return new ListProductsBySupermarketHandler(productAdapter);
  }

  @Bean
  public GetProductPriceHistoryUseCase getProductPriceHistoryUseCase(
      ProductPriceJpaAdapter priceAdapter) {
    return new GetProductPriceHistoryHandler(priceAdapter);
  }

  @Bean
  public ProductCacheEvictionListener productCacheEvictionListener(
      CachingGetProductByIdUseCase getProductByIdUseCase,
      CachingCompareProductsByNameUseCase compareProductsByNameUseCase) {
    return new ProductCacheEvictionListener(getProductByIdUseCase, compareProductsByNameUseCase);
  }
}
