package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.dto.AldiTopCategoryDto;
import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Fetches ALDI España categories from the internal front-end API.
 *
 * <p>The API returns a flat array of top-level categories, each containing an optional list of
 * sub-categories used for product fetching.
 *
 * <p>Category structure:
 *
 * <ul>
 *   <li>TOP — top-level nodes (e.g. "Frescos", "Bebidas")
 *   <li>SUB — leaf sub-category used as product query key (e.g. "Lácteos y huevos")
 * </ul>
 */
public class AldiCategoryScraperAdapter implements CategoryScraperPort {

  private static final Logger log = LoggerFactory.getLogger(AldiCategoryScraperAdapter.class);

  /** Seeded UUID for ALDI — see V3__seed_supermarkets.sql. */
  private static final UUID ALDI_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

  private static final ParameterizedTypeReference<List<AldiTopCategoryDto>> TOP_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient restClient;

  public AldiCategoryScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return ALDI_ID.equals(supermarketId.value());
  }

  @Override
  public List<RegisterCategoryCommand> fetchCategories(SupermarketId supermarketId) {
    var topCategories = fetchTopCategories();
    var commands = new ArrayList<RegisterCategoryCommand>();

    for (var top : topCategories) {
      commands.add(
          new RegisterCategoryCommand(top.name(), top.id(), supermarketId.value(), "TOP", null, 1));

      var subs = top.subcategories();
      if (subs == null) {
        continue;
      }
      for (var sub : subs) {
        commands.add(
            new RegisterCategoryCommand(
                sub.name(),
                sub.id(),
                supermarketId.value(),
                "SUB",
                null, // parentId UUID resolved after TOP categories are persisted
                2));
      }
    }

    log.info(
        "ALDI category scraper: {} commands for supermarket {}",
        commands.size(),
        supermarketId.value());
    return List.copyOf(commands);
  }

  private List<AldiTopCategoryDto> fetchTopCategories() {
    try {
      var result =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder.path("/api/front/v1/categories").queryParam("lang", "es").build())
              .retrieve()
              .body(TOP_LIST_TYPE);
      return result != null ? result : List.of();
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "ALDI categories API",
          "GET /api/front/v1/categories (HTTP " + ex.getStatusCode() + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException("ALDI categories API", "GET /api/front/v1/categories", ex);
    }
  }
}
