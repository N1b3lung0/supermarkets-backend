package com.n1b3lung0.supermarkets.comparison.infrastructure.config;

import com.n1b3lung0.supermarkets.comparison.application.query.CompareProductsByNameHandler;
import com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.cache.CachingCompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.output.persistence.ProductComparisonJdbcAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Wires together all Comparison beans. Zero Spring annotations in domain/application. */
@Configuration
public class ComparisonConfig {

  @Bean
  public ProductComparisonJdbcAdapter productComparisonJdbcAdapter(JdbcClient jdbcClient) {
    return new ProductComparisonJdbcAdapter(jdbcClient);
  }

  /**
   * Caching + metrics decorator that wraps the real handler. It implements {@link
   * com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase}
   * so it satisfies both the controller injection and the cache-eviction listener injection.
   */
  @Bean
  public CachingCompareProductsByNameUseCase compareProductsByNameUseCase(
      ProductComparisonJdbcAdapter adapter,
      CacheManager cacheManager,
      MeterRegistry meterRegistry) {
    return new CachingCompareProductsByNameUseCase(
        new CompareProductsByNameHandler(adapter), cacheManager, meterRegistry);
  }
}
