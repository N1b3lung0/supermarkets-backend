package com.n1b3lung0.supermarkets.aldi.infrastructure.config;

import com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.AldiCategoryScraperAdapter;
import com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.AldiProductScraperAdapter;
import com.n1b3lung0.supermarkets.aldi.infrastructure.config.properties.AldiScraperProperties;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the ALDI RestClient and scraper adapter beans. Domain/application classes have zero Spring
 * annotations — wiring happens here.
 */
@Configuration
@EnableConfigurationProperties(AldiScraperProperties.class)
public class AldiScraperConfig {

  @Bean("aldiRestClient")
  public RestClient aldiRestClient(AldiScraperProperties props) {
    return RestClient.builder()
        .baseUrl(props.baseUrl())
        .defaultHeader(
            "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
        .defaultHeader("Accept", "application/json")
        .defaultHeader("Accept-Language", "es-ES,es;q=0.9")
        .build();
  }

  @Bean
  public CategoryScraperPort aldiCategoryScraperAdapter(
      @Qualifier("aldiRestClient") RestClient restClient) {
    return new AldiCategoryScraperAdapter(restClient);
  }

  @Bean
  public ProductScraperPort aldiProductScraperAdapter(
      @Qualifier("aldiRestClient") RestClient restClient) {
    return new AldiProductScraperAdapter(restClient);
  }
}
