package com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper.dto.DiaProductDto;
import com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper.dto.DiaProductPageDto;
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
 * Fetches DIA España products for a given sub-category using the internal catalog API.
 *
 * <p>Pagination: iterates until {@code page >= totalPages}.
 */
public class DiaProductScraperAdapter implements ProductScraperPort {

  private static final Logger log = LoggerFactory.getLogger(DiaProductScraperAdapter.class);

  private static final int PAGE_SIZE = 48;

  /** Seeded UUID for DIA — see V3__seed_supermarkets.sql. */
  private static final UUID DIA_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");

  private final RestClient restClient;

  public DiaProductScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return DIA_ID.equals(supermarketId.value());
  }

  @Override
  public List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId subCategoryExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex) {

    var categoryId = leafCategoryIndex.get(subCategoryExternalId);
    if (categoryId == null) {
      log.warn(
          "No internal CategoryId for DIA subcategory={} — skipping",
          subCategoryExternalId.value());
      return List.of();
    }

    var commands = new ArrayList<UpsertProductCommand>();
    int currentPage = 0;
    int totalPages = 1;

    do {
      var page = fetchPage(subCategoryExternalId.value(), currentPage);
      if (page == null || page.products() == null) {
        break;
      }
      totalPages = page.totalPages();

      for (var product : page.products()) {
        commands.add(toCommand(product, supermarketId, categoryId));
      }

      log.debug(
          "DIA: fetched page {}/{} for subcategory={}",
          currentPage + 1,
          totalPages,
          subCategoryExternalId.value());
      currentPage++;

    } while (currentPage < totalPages);

    return List.copyOf(commands);
  }

  private DiaProductPageDto fetchPage(String categoryId, int page) {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/catalog/products")
                      .queryParam("categoryId", categoryId)
                      .queryParam("page", page)
                      .queryParam("size", PAGE_SIZE)
                      .build())
          .retrieve()
          .body(DiaProductPageDto.class);
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "DIA products API",
          "GET /api/catalog/products?categoryId="
              + categoryId
              + " (HTTP "
              + ex.getStatusCode()
              + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "DIA products API", "GET /api/catalog/products?categoryId=" + categoryId, ex);
    }
  }

  private UpsertProductCommand toCommand(
      DiaProductDto dto, SupermarketId supermarketId, CategoryId categoryId) {

    var unitPrice =
        dto.price() != null
            ? Money.of(dto.price(), dto.currency() != null ? dto.currency() : "EUR")
            : Money.ofEur(BigDecimal.ZERO);

    return new UpsertProductCommand(
        dto.id(),
        supermarketId.value(),
        categoryId.value(),
        dto.name(),
        null,
        null,
        dto.brand(),
        null,
        null,
        null,
        dto.imageUrl(),
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
