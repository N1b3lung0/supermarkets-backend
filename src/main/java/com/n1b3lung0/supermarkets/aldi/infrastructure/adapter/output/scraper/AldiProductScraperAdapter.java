package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.dto.AldiProductDto;
import com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.dto.AldiProductPageDto;
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
 * Fetches ALDI España products for a given sub-category using the internal front-end API.
 *
 * <p>Pagination: iterates all pages until {@code page >= totalPages}.
 */
public class AldiProductScraperAdapter implements ProductScraperPort {

  private static final Logger log = LoggerFactory.getLogger(AldiProductScraperAdapter.class);

  private static final int PAGE_SIZE = 48;

  /** Seeded UUID for ALDI — see V3__seed_supermarkets.sql. */
  private static final UUID ALDI_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

  private final RestClient restClient;

  public AldiProductScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return ALDI_ID.equals(supermarketId.value());
  }

  @Override
  public List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId subCategoryExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex) {

    var categoryId = leafCategoryIndex.get(subCategoryExternalId);
    if (categoryId == null) {
      log.warn(
          "No internal CategoryId for ALDI subcategory={} — skipping",
          subCategoryExternalId.value());
      return List.of();
    }

    var commands = new ArrayList<UpsertProductCommand>();
    int currentPage = 0;
    int totalPages = 1;

    do {
      var page = fetchPage(subCategoryExternalId.value(), currentPage);
      if (page == null || page.content() == null) {
        break;
      }
      totalPages = page.totalPages();

      for (var product : page.content()) {
        commands.add(toCommand(product, supermarketId, categoryId));
      }

      log.debug(
          "ALDI: fetched page {}/{} for subcategory={}",
          currentPage + 1,
          totalPages,
          subCategoryExternalId.value());
      currentPage++;

    } while (currentPage < totalPages);

    return List.copyOf(commands);
  }

  private AldiProductPageDto fetchPage(String categoryId, int page) {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/front/v1/products")
                      .queryParam("categoryId", categoryId)
                      .queryParam("lang", "es")
                      .queryParam("page", page)
                      .queryParam("size", PAGE_SIZE)
                      .build())
          .retrieve()
          .body(AldiProductPageDto.class);
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "ALDI products API",
          "GET /api/front/v1/products?categoryId="
              + categoryId
              + "&page="
              + page
              + " (HTTP "
              + ex.getStatusCode()
              + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "ALDI products API",
          "GET /api/front/v1/products?categoryId=" + categoryId + "&page=" + page,
          ex);
    }
  }

  private UpsertProductCommand toCommand(
      AldiProductDto dto, SupermarketId supermarketId, CategoryId categoryId) {

    var unitPrice =
        dto.price() != null
            ? Money.of(dto.price(), dto.currency() != null ? dto.currency() : "EUR")
            : Money.ofEur(BigDecimal.ZERO);

    var priceInstructions = PriceInstructions.unitOnly(unitPrice);

    return new UpsertProductCommand(
        dto.id(), // externalId
        supermarketId.value(),
        categoryId.value(),
        dto.name(),
        null, // legalName
        null, // description
        dto.brand(), // brand
        dto.ean(), // ean
        null, // origin
        null, // packaging
        dto.imageUrl(), // thumbnailUrl
        null, // storageInstructions
        null, // usageInstructions
        null, // mandatoryMentions
        null, // productionVariant
        null, // dangerMentions
        null, // allergens
        null, // ingredients
        List.of(), // supplierNames
        false, // isWater
        false, // requiresAgeCheck
        false, // isBulk
        false, // isVariableWeight
        0, // purchaseLimit
        priceInstructions);
  }
}
