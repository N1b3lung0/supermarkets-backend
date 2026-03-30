package com.n1b3lung0.supermarkets.comparison.infrastructure.config;

import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.comparison.application.query.CompareProductsByNameHandler;
import com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.output.persistence.ProductComparisonJdbcAdapter;
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

  @Bean
  public CompareProductsByNameUseCase compareProductsByNameUseCase(
      ProductComparisonJdbcAdapter adapter) {
    return new CompareProductsByNameHandler(adapter);
  }
}
