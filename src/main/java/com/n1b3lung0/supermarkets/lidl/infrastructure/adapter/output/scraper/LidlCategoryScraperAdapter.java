package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto.LidlCategoryDto;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Fetches LIDL España categories from the internal API.
 *
 * <p>LIDL España uses a flat category list of assortment slugs (no sub-categories). All categories
 * are registered as TOP level.
 */
public class LidlCategoryScraperAdapter implements CategoryScraperPort {

  private static final Logger log = LoggerFactory.getLogger(LidlCategoryScraperAdapter.class);

  /** Seeded UUID for LIDL — see V3__seed_supermarkets.sql. */
  private static final UUID LIDL_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

  private static final ParameterizedTypeReference<List<LidlCategoryDto>> CATEGORY_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient restClient;

  public LidlCategoryScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return LIDL_ID.equals(supermarketId.value());
  }

  @Override
  public List<RegisterCategoryCommand> fetchCategories(SupermarketId supermarketId) {
    var categories = fetchCategoryList();
    var commands =
        categories.stream()
            .map(
                cat ->
                    new RegisterCategoryCommand(
                        cat.name(), cat.id(), supermarketId.value(), "TOP", null, 1))
            .toList();

    log.info(
        "LIDL category scraper: {} commands for supermarket {}",
        commands.size(),
        supermarketId.value());
    return commands;
  }

  private List<LidlCategoryDto> fetchCategoryList() {
    try {
      var result =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder.path("/api/categories/lidl").queryParam("lang", "es").build())
              .retrieve()
              .body(CATEGORY_LIST_TYPE);
      return result != null ? result : List.of();
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "LIDL categories API", "GET /api/categories/lidl (HTTP " + ex.getStatusCode() + ")", ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException("LIDL categories API", "GET /api/categories/lidl", ex);
    }
  }
}
