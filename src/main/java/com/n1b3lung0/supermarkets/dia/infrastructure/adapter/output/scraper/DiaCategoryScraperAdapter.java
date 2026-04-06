package com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper.dto.DiaCategoryDto;
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
 * Fetches DIA España categories from the internal catalog API.
 *
 * <p>Category structure: TOP → SUB (children list, no further nesting).
 */
public class DiaCategoryScraperAdapter implements CategoryScraperPort {

  private static final Logger log = LoggerFactory.getLogger(DiaCategoryScraperAdapter.class);

  /** Seeded UUID for DIA — see V3__seed_supermarkets.sql. */
  private static final UUID DIA_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");

  private static final ParameterizedTypeReference<List<DiaCategoryDto>> CAT_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  private final RestClient restClient;

  public DiaCategoryScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return DIA_ID.equals(supermarketId.value());
  }

  @Override
  public List<RegisterCategoryCommand> fetchCategories(SupermarketId supermarketId) {
    var categories = fetchCategoryList();
    var commands = new ArrayList<RegisterCategoryCommand>();

    for (var top : categories) {
      commands.add(
          new RegisterCategoryCommand(top.name(), top.id(), supermarketId.value(), "TOP", null, 1));
      if (top.children() != null) {
        for (var sub : top.children()) {
          commands.add(
              new RegisterCategoryCommand(
                  sub.name(), sub.id(), supermarketId.value(), "SUB", null, 2));
        }
      }
    }

    log.info(
        "DIA category scraper: {} commands for supermarket {}",
        commands.size(),
        supermarketId.value());
    return List.copyOf(commands);
  }

  private List<DiaCategoryDto> fetchCategoryList() {
    try {
      var result =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder.path("/api/catalog/categories").queryParam("lang", "es").build())
              .retrieve()
              .body(CAT_LIST_TYPE);
      return result != null ? result : List.of();
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "DIA categories API",
          "GET /api/catalog/categories (HTTP " + ex.getStatusCode() + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException("DIA categories API", "GET /api/catalog/categories", ex);
    }
  }
}
