package com.n1b3lung0.supermarkets.alcampo.infrastructure.config;

import com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.AlcampoCategoryScraperAdapter;
import com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.AlcampoProductScraperAdapter;
import com.n1b3lung0.supermarkets.alcampo.infrastructure.config.properties.AlcampoScraperProperties;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires the Alcampo RestClient and scraper adapter beans. Domain/application classes have zero
 * Spring annotations — wiring happens here.
 */
@Configuration
@EnableConfigurationProperties(AlcampoScraperProperties.class)
public class AlcampoScraperConfig {

  @Bean("alcampoRestClient")
  public RestClient alcampoRestClient(AlcampoScraperProperties props) {
    return RestClient.builder()
        .baseUrl(props.baseUrl())
        .defaultHeader(
            "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
        .defaultHeader("Accept", "application/json")
        .defaultHeader("Accept-Language", "es-ES,es;q=0.9")
        .build();
  }

  @Bean
  public CategoryScraperPort alcampoCategoryScraperAdapter(
      @Qualifier("alcampoRestClient") RestClient restClient) {
    return new AlcampoCategoryScraperAdapter(restClient);
  }

  @Bean
  public ProductScraperPort alcampoProductScraperAdapter(
      @Qualifier("alcampoRestClient") RestClient restClient) {
    return new AlcampoProductScraperAdapter(restClient);
  }
}
