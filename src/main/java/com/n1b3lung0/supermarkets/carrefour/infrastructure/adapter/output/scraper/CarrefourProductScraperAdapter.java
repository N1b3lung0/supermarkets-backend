package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto.CarrefourProductDto;
import com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto.CarrefourProductSearchResponse;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.product.domain.model.PriceInstructions;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.port.output.scraper.ProductScraperPort;
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
 * Fetches Carrefour España products for a given sub-category (leaf uid).
 *
 * <p>Pagination: iterates all pages until {@code currentPage >= totalPages - 1}.
 */
public class CarrefourProductScraperAdapter implements ProductScraperPort {

  private static final Logger log = LoggerFactory.getLogger(CarrefourProductScraperAdapter.class);

  private static final int PAGE_SIZE = 48;

  /** Seeded UUID for Carrefour — see V3__seed_supermarkets.sql. */
  private static final UUID CARREFOUR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private final RestClient restClient;

  public CarrefourProductScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return CARREFOUR_ID.equals(supermarketId.value());
  }

  @Override
  public List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId subCategoryExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex) {

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
        var categoryId = resolveCategoryId(product, leafCategoryIndex);
        if (categoryId == null) {
          log.warn(
              "No internal CategoryId for Carrefour product code={} categories={} — skipping",
              product.code(),
              product.categories());
          continue;
        }
        commands.add(toCommand(product, supermarketId, categoryId));
      }

      log.debug(
          "Carrefour: fetched page {}/{} for subcategory={}",
          currentPage + 1,
          totalPages,
          subCategoryExternalId.value());
      currentPage++;

    } while (currentPage < totalPages);

    return List.copyOf(commands);
  }

  private CategoryId resolveCategoryId(
      CarrefourProductDto product, Map<ExternalCategoryId, CategoryId> index) {
    if (product.categories() == null || product.categories().isEmpty()) {
      return null;
    }
    for (var ref : product.categories()) {
      var id = index.get(ExternalCategoryId.of(ref.code()));
      if (id != null) {
        return id;
      }
    }
    return null;
  }

  private CarrefourProductSearchResponse fetchPage(String categoryCode, int page) {
    var query = ":relevance:allCategories:" + categoryCode;
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/2.0/products/search")
                      .queryParam("query", query)
                      .queryParam("currentPage", page)
                      .queryParam("pageSize", PAGE_SIZE)
                      .queryParam("lang", "es_ES")
                      .queryParam("curr", "EUR")
                      .build())
          .retrieve()
          .body(CarrefourProductSearchResponse.class);
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "Carrefour products API",
          "GET /api/2.0/products/search?category="
              + categoryCode
              + "&page="
              + page
              + " (HTTP "
              + ex.getStatusCode()
              + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "Carrefour products API",
          "GET /api/2.0/products/search?category=" + categoryCode + "&page=" + page,
          ex);
    }
  }

  private UpsertProductCommand toCommand(
      CarrefourProductDto dto, SupermarketId supermarketId, CategoryId categoryId) {

    var price = dto.price();
    var unitPrice =
        price != null
            ? Money.of(price.value(), price.currencyIso() != null ? price.currencyIso() : "EUR")
            : Money.ofEur(java.math.BigDecimal.ZERO);

    var thumbnail =
        dto.images() != null && !dto.images().isEmpty() ? dto.images().get(0).url() : null;

    // Use unitOnly() factory — Carrefour API does not expose all PriceInstructions fields
    var priceInstructions = PriceInstructions.unitOnly(unitPrice);

    return new UpsertProductCommand(
        dto.code(), // externalId
        supermarketId.value(),
        categoryId.value(),
        dto.name(), // name
        null, // legalName
        null, // description
        null, // brand
        null, // ean
        null, // origin
        null, // packaging
        thumbnail, // thumbnailUrl
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
