package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto.CarrefourPageResponse;
import com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.mapper.CarrefourCategoryMapper;
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
 * Fetches Carrefour España category tree from the internal SAP Hybris API.
 *
 * <p>The API is protected by Cloudflare. At runtime a 403 response causes a {@link
 * ExternalServiceException} so the SyncRun records FAILED gracefully.
 *
 * <p>Category structure:
 *
 * <ul>
 *   <li>TOP — top-level nodes (e.g. "Alimentación", "Bebidas")
 *   <li>SUB — leaf children of each TOP node (e.g. "Lácteos y huevos")
 * </ul>
 */
public class CarrefourCategoryScraperAdapter implements CategoryScraperPort {

  private static final Logger log = LoggerFactory.getLogger(CarrefourCategoryScraperAdapter.class);

  private static final String CATEGORY_COMPONENT_UID = "CategoryNavigationComponent";

  /** Seeded UUID for Carrefour — see V3__seed_supermarkets.sql. */
  private static final UUID CARREFOUR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private final RestClient restClient;
  private final CarrefourCategoryMapper mapper;

  public CarrefourCategoryScraperAdapter(RestClient restClient, CarrefourCategoryMapper mapper) {
    this.restClient = restClient;
    this.mapper = mapper;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return CARREFOUR_ID.equals(supermarketId.value());
  }

  @Override
  public List<RegisterCategoryCommand> fetchCategories(SupermarketId supermarketId) {
    var page = fetchPage();
    var commands = new ArrayList<RegisterCategoryCommand>();

    var navComponent =
        page.components().stream()
            .filter(c -> CATEGORY_COMPONENT_UID.equals(c.uid()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ExternalServiceException(
                        "Carrefour categories API",
                        "GET /api/2.0/page",
                        new IllegalStateException(
                            "CategoryNavigationComponent not found in response")));

    var topNodes = navComponent.navigationNode().children();
    for (var top : topNodes) {
      commands.add(mapper.toTopCommand(top, supermarketId));
      for (var sub : top.children()) {
        commands.add(mapper.toSubCommand(sub, supermarketId));
      }
    }

    log.info(
        "Carrefour category scraper: {} commands for supermarket {}",
        commands.size(),
        supermarketId.value());
    return List.copyOf(commands);
  }

  private CarrefourPageResponse fetchPage() {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/2.0/page")
                      .queryParam("path", "/supermercado")
                      .queryParam("locale", "es_ES")
                      .build())
          .retrieve()
          .body(CarrefourPageResponse.class);
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "Carrefour categories API", "GET /api/2.0/page (HTTP " + ex.getStatusCode() + ")", ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException("Carrefour categories API", "GET /api/2.0/page", ex);
    }
  }
}
