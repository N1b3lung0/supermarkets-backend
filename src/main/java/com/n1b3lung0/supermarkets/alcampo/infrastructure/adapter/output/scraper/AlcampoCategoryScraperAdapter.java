package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto.AlcampoCategoriesResponse;
import com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto.AlcampoCategoryNodeDto;
import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Fetches Alcampo España categories from the SAP Commerce API.
 *
 * <p>Category structure:
 *
 * <ul>
 *   <li>TOP — top-level nodes (e.g. "Alimentación", "Bebidas")
 *   <li>SUB — leaf sub-categories used for product fetching (e.g. "Lácteos y huevos")
 * </ul>
 */
public class AlcampoCategoryScraperAdapter implements CategoryScraperPort {

  private static final Logger log = LoggerFactory.getLogger(AlcampoCategoryScraperAdapter.class);

  /** Seeded UUID for Alcampo — see V3__seed_supermarkets.sql. */
  private static final UUID ALCAMPO_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private final RestClient restClient;

  public AlcampoCategoryScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return ALCAMPO_ID.equals(supermarketId.value());
  }

  @Override
  public List<RegisterCategoryCommand> fetchCategories(SupermarketId supermarketId) {
    var response = fetchCategoryTree();
    var commands = new ArrayList<RegisterCategoryCommand>();

    if (response == null || response.subcategories() == null) {
      return List.of();
    }

    for (var top : response.subcategories()) {
      commands.add(toCommand(top, supermarketId, "TOP", null, 1));
      if (top.subcategories() != null) {
        for (var sub : top.subcategories()) {
          commands.add(toCommand(sub, supermarketId, "SUB", top.id(), 2));
        }
      }
    }

    log.info(
        "Alcampo category scraper: {} commands for supermarket {}",
        commands.size(),
        supermarketId.value());
    return List.copyOf(commands);
  }

  private RegisterCategoryCommand toCommand(
      AlcampoCategoryNodeDto dto,
      SupermarketId supermarketId,
      String levelType,
      String parentExternalId,
      int order) {
    return new RegisterCategoryCommand(
        dto.name(), dto.id(), supermarketId.value(), levelType, parentExternalId, order);
  }

  private AlcampoCategoriesResponse fetchCategoryTree() {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder.path("/api/v2/alcampo/categories").queryParam("lang", "es").build())
          .retrieve()
          .body(AlcampoCategoriesResponse.class);
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "Alcampo categories API",
          "GET /api/v2/alcampo/categories (HTTP " + ex.getStatusCode() + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "Alcampo categories API", "GET /api/v2/alcampo/categories", ex);
    }
  }
}
