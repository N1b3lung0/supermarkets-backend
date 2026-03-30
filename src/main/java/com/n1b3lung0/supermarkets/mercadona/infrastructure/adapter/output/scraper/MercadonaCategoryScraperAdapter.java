package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaCategoriesResponse;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaLevel1DetailDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper.MercadonaCategoryMapper;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.CategoryScraperPort;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls Mercadona's internal categories API and converts the 3-level tree into a flat list of
 * {@link RegisterCategoryCommand} objects ordered parents-before-children.
 *
 * <p>Flow:
 *
 * <ol>
 *   <li>GET /categories/ → 26 TOP categories, each with their level-1 ids
 *   <li>For each level-1 id → GET /categories/{id} → leaf groups
 *   <li>Emit TOP, then SUB, then LEAF commands
 * </ol>
 */
public class MercadonaCategoryScraperAdapter implements CategoryScraperPort {

  private static final Logger log = LoggerFactory.getLogger(MercadonaCategoryScraperAdapter.class);

  private final RestClient restClient;
  private final MercadonaCategoryMapper mapper;

  public MercadonaCategoryScraperAdapter(RestClient restClient, MercadonaCategoryMapper mapper) {
    this.restClient = restClient;
    this.mapper = mapper;
  }

  @Override
  public List<RegisterCategoryCommand> fetchCategories(SupermarketId supermarketId) {
    var commands = new ArrayList<RegisterCategoryCommand>();
    var response = getCategories();

    for (var top : response.results()) {
      commands.add(mapper.toTopCommand(top, supermarketId));
      for (var sub : top.categories()) {
        commands.add(mapper.toSubCommand(sub, supermarketId, String.valueOf(top.id())));
        try {
          var detail = getLevel1Detail(sub.id());
          for (var leaf : detail.categories()) {
            commands.add(mapper.toLeafCommand(leaf, supermarketId, null));
          }
        } catch (RestClientException ex) {
          log.warn("Failed to fetch subcategory id={}: {}", sub.id(), ex.getMessage());
          throw new ExternalServiceException(
              "Mercadona categories API", "GET /categories/" + sub.id(), ex);
        }
      }
    }

    log.info(
        "Mercadona category scraper: {} commands for supermarket {}",
        commands.size(),
        supermarketId.value());
    return List.copyOf(commands);
  }

  private MercadonaCategoriesResponse getCategories() {
    try {
      return restClient
          .get()
          .uri("/categories/")
          .retrieve()
          .body(MercadonaCategoriesResponse.class);
    } catch (RestClientException ex) {
      throw new ExternalServiceException("Mercadona categories API", "GET /categories/", ex);
    }
  }

  private MercadonaLevel1DetailDto getLevel1Detail(int subcategoryId) {
    return restClient
        .get()
        .uri("/categories/{id}", subcategoryId)
        .retrieve()
        .body(MercadonaLevel1DetailDto.class);
  }
}
