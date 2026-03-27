package com.n1b3lung0.supermarkets.mercadona.infrastructure.config;

import com.n1b3lung0.supermarkets.mercadona.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.mercadona.application.port.output.scraper.ProductScraperPort;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.MercadonaCategoryScraperAdapter;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.MercadonaProductScraperAdapter;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper.MercadonaCategoryMapper;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper.MercadonaPriceInstructionsMapper;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.config.properties.MercadonaScraperProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the Mercadona RestClient with base URL and default headers, and creates all scraper adapter
 * beans. Domain/application classes have zero Spring annotations.
 */
@Configuration
@EnableConfigurationProperties(MercadonaScraperProperties.class)
public class MercadonaScraperConfig {

  @Bean("mercadonaRestClient")
  public RestClient mercadonaRestClient(MercadonaScraperProperties props) {
    return RestClient.builder()
        .baseUrl(props.baseUrl())
        .defaultHeader("User-Agent", "Mozilla/5.0")
        .defaultHeader("Accept", "application/json")
        .build();
  }

  @Bean
  public MercadonaCategoryMapper mercadonaCategoryMapper() {
    return new MercadonaCategoryMapper();
  }

  @Bean
  public MercadonaPriceInstructionsMapper mercadonaPriceInstructionsMapper() {
    return new MercadonaPriceInstructionsMapper();
  }

  @Bean
  public CategoryScraperPort mercadonaCategoryScraperAdapter(
      @Qualifier("mercadonaRestClient") RestClient restClient,
      MercadonaCategoryMapper categoryMapper) {
    return new MercadonaCategoryScraperAdapter(restClient, categoryMapper);
  }

  @Bean
  public ProductScraperPort mercadonaProductScraperAdapter(
      @Qualifier("mercadonaRestClient") RestClient restClient,
      MercadonaPriceInstructionsMapper priceMapper) {
    return new MercadonaProductScraperAdapter(restClient, priceMapper);
  }
}
