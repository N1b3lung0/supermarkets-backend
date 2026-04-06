package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto.LidlGridBoxDto;
import com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto.LidlGridBoxPageDto;
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
 * Fetches LIDL España products for a given assortment (category slug) using the gridboxes API.
 *
 * <p>Pagination: iterates until {@code page * pageSize >= totalCount}.
 */
public class LidlProductScraperAdapter implements ProductScraperPort {

  private static final Logger log = LoggerFactory.getLogger(LidlProductScraperAdapter.class);

  private static final int PAGE_SIZE = 48;

  /** Seeded UUID for LIDL — see V3__seed_supermarkets.sql. */
  private static final UUID LIDL_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

  private final RestClient restClient;

  public LidlProductScraperAdapter(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return LIDL_ID.equals(supermarketId.value());
  }

  @Override
  public List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId subCategoryExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex) {

    var categoryId = leafCategoryIndex.get(subCategoryExternalId);
    if (categoryId == null) {
      log.warn(
          "No internal CategoryId for LIDL category={} — skipping", subCategoryExternalId.value());
      return List.of();
    }

    var commands = new ArrayList<UpsertProductCommand>();
    int currentPage = 0;

    while (true) {
      var page = fetchPage(subCategoryExternalId.value(), currentPage);
      if (page == null || page.gridBoxes() == null || page.gridBoxes().isEmpty()) {
        break;
      }

      for (var product : page.gridBoxes()) {
        commands.add(toCommand(product, supermarketId, categoryId));
      }

      log.debug(
          "LIDL: fetched page {} for category={}, total={}",
          currentPage,
          subCategoryExternalId.value(),
          page.totalCount());

      // Check if we've fetched all products
      if ((long) (currentPage + 1) * PAGE_SIZE >= page.totalCount()) {
        break;
      }
      currentPage++;
    }

    return List.copyOf(commands);
  }

  private LidlGridBoxPageDto fetchPage(String assortment, int page) {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/api/gridboxes/ES/es")
                      .queryParam("assortments", assortment)
                      .queryParam("page", page)
                      .queryParam("pageSize", PAGE_SIZE)
                      .build())
          .retrieve()
          .body(LidlGridBoxPageDto.class);
    } catch (RestClientResponseException ex) {
      throw new ExternalServiceException(
          "LIDL products API",
          "GET /api/gridboxes/ES/es?assortments="
              + assortment
              + "&page="
              + page
              + " (HTTP "
              + ex.getStatusCode()
              + ")",
          ex);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "LIDL products API",
          "GET /api/gridboxes/ES/es?assortments=" + assortment + "&page=" + page,
          ex);
    }
  }

  private UpsertProductCommand toCommand(
      LidlGridBoxDto dto, SupermarketId supermarketId, CategoryId categoryId) {

    var priceValue =
        dto.price() != null && dto.price().price() != null ? dto.price().price() : BigDecimal.ZERO;
    var currency =
        dto.price() != null && dto.price().currency() != null ? dto.price().currency() : "EUR";
    var unitPrice = Money.of(priceValue, currency);
    var priceInstructions = PriceInstructions.unitOnly(unitPrice);

    var brand = dto.keyfacts() != null ? dto.keyfacts().brand() : null;

    return new UpsertProductCommand(
        dto.id(),
        supermarketId.value(),
        categoryId.value(),
        dto.fullTitle(),
        null, // legalName
        null, // description
        brand,
        null, // ean
        null, // origin
        null, // packaging
        dto.image(),
        null, // storageInstructions
        null, // usageInstructions
        null, // mandatoryMentions
        null, // productionVariant
        null, // dangerMentions
        null, // allergens
        null, // ingredients
        List.of(),
        false,
        false,
        false,
        false,
        0,
        priceInstructions);
  }
}
