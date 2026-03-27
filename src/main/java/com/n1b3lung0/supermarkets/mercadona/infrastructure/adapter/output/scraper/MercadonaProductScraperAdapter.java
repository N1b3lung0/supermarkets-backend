package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.mercadona.application.port.output.scraper.ProductScraperPort;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaLevel1DetailDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaProductInCategoryDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper.MercadonaPriceInstructionsMapper;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Fetches all products within a level-1 subcategory and maps them to {@link UpsertProductCommand}.
 * Fields not available in the categories endpoint (ean, legalName, brand, origin, details,
 * allergens, ingredients, isBulk, isVariableWeight) are set to null and can be enriched later via
 * Step 52b.
 */
public class MercadonaProductScraperAdapter implements ProductScraperPort {

  private static final Logger log = LoggerFactory.getLogger(MercadonaProductScraperAdapter.class);

  private final RestClient restClient;
  private final MercadonaPriceInstructionsMapper priceMapper;

  public MercadonaProductScraperAdapter(
      RestClient restClient, MercadonaPriceInstructionsMapper priceMapper) {
    this.restClient = restClient;
    this.priceMapper = priceMapper;
  }

  @Override
  public List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId level1ExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex) {

    MercadonaLevel1DetailDto detail;
    try {
      detail =
          restClient
              .get()
              .uri("/categories/{id}", level1ExternalId.value())
              .retrieve()
              .body(MercadonaLevel1DetailDto.class);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "Mercadona products API", "GET /categories/" + level1ExternalId.value(), ex);
    }

    if (detail == null) {
      return List.of();
    }

    var commands = new ArrayList<UpsertProductCommand>();

    for (var leafGroup : detail.categories()) {
      var leafExtId = ExternalCategoryId.of(String.valueOf(leafGroup.id()));
      var categoryId = leafCategoryIndex.get(leafExtId);

      if (categoryId == null) {
        log.warn(
            "No internal CategoryId found for leaf group externalId={} — skipping {} products",
            leafGroup.id(),
            leafGroup.products().size());
        continue;
      }

      for (var product : leafGroup.products()) {
        commands.add(toCommand(product, supermarketId, categoryId));
      }
    }

    log.debug(
        "Fetched {} products from subcategory {} for supermarket {}",
        commands.size(),
        level1ExternalId.value(),
        supermarketId.value());

    return List.copyOf(commands);
  }

  private UpsertProductCommand toCommand(
      MercadonaProductInCategoryDto dto, SupermarketId supermarketId, CategoryId categoryId) {

    var priceInstructions = priceMapper.toDomain(dto.priceInstructions());

    return new UpsertProductCommand(
        dto.id(),
        supermarketId.value(),
        categoryId.value(),
        dto.displayName(),
        null, // legalName — not in categories endpoint
        null, // description — not in categories endpoint
        null, // brand — not in categories endpoint
        null, // ean — not in categories endpoint
        null, // origin — not in categories endpoint
        dto.packaging(),
        dto.thumbnail(),
        null, // storageInstructions — not in categories endpoint
        null, // usageInstructions — not in categories endpoint
        null, // mandatoryMentions — not in categories endpoint
        null, // productionVariant — not in categories endpoint
        null, // dangerMentions — not in categories endpoint
        null, // allergens — not in categories endpoint
        null, // ingredients — not in categories endpoint
        List.of(), // suppliers — not in categories endpoint
        dto.badges() != null && dto.badges().isWater(),
        dto.badges() != null && dto.badges().requiresAgeCheck(),
        false, // isBulk — not in categories endpoint
        false, // isVariableWeight — not in categories endpoint
        dto.limit(),
        priceInstructions);
  }
}
