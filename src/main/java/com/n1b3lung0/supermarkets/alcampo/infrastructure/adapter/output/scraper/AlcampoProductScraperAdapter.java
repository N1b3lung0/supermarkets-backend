package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto.AlcampoProductDto;
import com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto.AlcampoProductSearchResponse;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.product.domain.model.PriceInstructions;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Fetches Alcampo España products for a given sub-category using the SAP Commerce search API.
 *
 * <p>Pagination: iterates all pages until {@code currentPage >= totalPages}.
 */
public class AlcampoProductScraperAdapter implements ProductScraperPort {

  private static final Logger log = LoggerFactory.getLogger(AlcampoProductScraperAdapter.class);

  private static final int PAGE_SIZE = 48;

  /** Seeded UUID for Alcampo — see V3__seed_supermarkets.sql. */
  private static final UUID ALCAMPO_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private final RestClient restClient;

  public AlcampoProductScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return ALCAMPO_ID.equals(supermarketId.value());
  }

  @Override
  public List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId subCategoryExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex) {

    var categoryId = leafCategoryIndex.get(subCategoryExternalId);
    if (categoryId == null) {
      log.warn(
          "No internal CategoryId for Alcampo subcategory={} — skipping",
          subCategoryExternalId.value());
      return List.of();
    }

    var commands = new ArrayList<UpsertProductCommand>();
    int currentPage = 0;
    int totalPages = 1;

    do {
      var response = fetchPage(subCategoryExternalId.value(), currentPage);
      if (response == null || response.products() == null) {
        break;
      }
      totalPages = response.pagination().totalPages();

      for (var product : response.products()) {
        commands.add(toCommand(product, supermarketId, categoryId));
      }

      log.debug(
          "Alcampo: fetched page {}/{} for subcategory={}",
          currentPage + 1,
          totalPages,
          subCategoryExternalId.value());
      currentPage++;

    } while (currentPage < totalPages);

    return List.copyOf(commands);
  }

  private AlcampoProductSearchResponse fetchPage(String categoryCode, int page) {
    var query = ":relevance:allCategories:" + categoryCode;
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/v2/alcampo/products/search")
                      .queryParam("query", query)
                      .queryParam("currentPage", page)
                      .queryParam("pageSize", PAGE_SIZE)
                      .queryParam("lang", "es")
                      .build())
          .retrieve()
          .body(AlcampoProductSearchResponse.class);
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "Alcampo products API",
          "GET /api/v2/alcampo/products/search?category="
              + categoryCode
              + " (HTTP "
              + ex.getStatusCode()
              + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "Alcampo products API",
          "GET /api/v2/alcampo/products/search?category=" + categoryCode,
          ex);
    }
  }

  private UpsertProductCommand toCommand(
      AlcampoProductDto dto, SupermarketId supermarketId, CategoryId categoryId) {

    var unitPrice =
        dto.price() != null && dto.price().value() != null
            ? Money.of(
                dto.price().value(),
                dto.price().currencyIso() != null ? dto.price().currencyIso() : "EUR")
            : Money.ofEur(BigDecimal.ZERO);

    var thumbnail =
        dto.images() != null && !dto.images().isEmpty() ? dto.images().get(0).url() : null;

    return new UpsertProductCommand(
        dto.code(),
        supermarketId.value(),
        categoryId.value(),
        dto.name(),
        null,
        null,
        null,
        null,
        null,
        null,
        thumbnail,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        List.of(),
        false,
        false,
        false,
        false,
        0,
        PriceInstructions.unitOnly(unitPrice));
  }
}
