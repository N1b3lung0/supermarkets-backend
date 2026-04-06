package com.n1b3lung0.supermarkets.lidl.infrastructure.config;

import com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.LidlCategoryScraperAdapter;
import com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.LidlProductScraperAdapter;
import com.n1b3lung0.supermarkets.lidl.infrastructure.config.properties.LidlScraperProperties;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the LIDL RestClient and scraper adapter beans. Domain/application classes have zero Spring
 * annotations — wiring happens here.
 */
@Configuration
@EnableConfigurationProperties(LidlScraperProperties.class)
public class LidlScraperConfig {

  @Bean("lidlRestClient")
  public RestClient lidlRestClient(LidlScraperProperties props) {
    return RestClient.builder()
        .baseUrl(props.baseUrl())
        .defaultHeader(
            "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
        .defaultHeader("Accept", "application/json")
        .defaultHeader("Accept-Language", "es-ES,es;q=0.9")
        .build();
  }

  @Bean
  public CategoryScraperPort lidlCategoryScraperAdapter(
      @Qualifier("lidlRestClient") RestClient restClient) {
    return new LidlCategoryScraperAdapter(restClient);
  }

  @Bean
  public ProductScraperPort lidlProductScraperAdapter(
      @Qualifier("lidlRestClient") RestClient restClient) {
    return new LidlProductScraperAdapter(restClient);
  }
}
