package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaLevel1DetailDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaProductDetailDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaProductInCategoryDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaSupplierDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper.MercadonaPriceInstructionsMapper;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
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

/**
 * Fetches all products within a level-1 subcategory and maps them to {@link UpsertProductCommand}.
 * First calls GET /categories/{id} to get the product list, then enriches each product with a call
 * to GET /products/{id} to populate ean, legalName, brand, origin, details, allergens, ingredients,
 * isBulk and isVariableWeight.
 */
public class MercadonaProductScraperAdapter implements ProductScraperPort {

  private static final Logger log = LoggerFactory.getLogger(MercadonaProductScraperAdapter.class);

  /** Seeded UUID for Mercadona — see V3__seed_supermarkets.sql. */
  private static final UUID MERCADONA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private final RestClient restClient;
  private final MercadonaPriceInstructionsMapper priceMapper;

  public MercadonaProductScraperAdapter(
      RestClient restClient, MercadonaPriceInstructionsMapper priceMapper) {
    this.restClient = restClient;
    this.priceMapper = priceMapper;
  }

  @Override
  public boolean supports(SupermarketId supermarketId) {
    return MERCADONA_ID.equals(supermarketId.value());
  }

  @Override
  public List<UpsertProductCommand> fetchProductsBySubcategory(
      SupermarketId supermarketId,
      ExternalCategoryId level1ExternalId,
      Map<ExternalCategoryId, CategoryId> leafCategoryIndex) {

    MercadonaLevel1DetailDto subcategoryDetail;
    try {
      subcategoryDetail =
          restClient
              .get()
              .uri("/categories/{id}", level1ExternalId.value())
              .retrieve()
              .body(MercadonaLevel1DetailDto.class);
    } catch (RestClientException ex) {
      throw new ExternalServiceException(
          "Mercadona products API", "GET /categories/" + level1ExternalId.value(), ex);
    }

    if (subcategoryDetail == null) {
      return List.of();
    }

    var commands = new ArrayList<UpsertProductCommand>();

    for (var leafGroup : subcategoryDetail.categories()) {
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
        var detail = fetchProductDetail(product.id());
        commands.add(toCommand(product, detail, supermarketId, categoryId));
      }
    }

    log.debug(
        "Fetched {} products from subcategory {} for supermarket {}",
        commands.size(),
        level1ExternalId.value(),
        supermarketId.value());

    return List.copyOf(commands);
  }

  private MercadonaProductDetailDto fetchProductDetail(String productId) {
    try {
      return restClient
          .get()
          .uri("/products/{id}", productId)
          .retrieve()
          .body(MercadonaProductDetailDto.class);
    } catch (RestClientException ex) {
      log.warn("Could not fetch detail for product {} — enriched fields will be null", productId);
      return null;
    }
  }

  private UpsertProductCommand toCommand(
      MercadonaProductInCategoryDto dto,
      MercadonaProductDetailDto detail,
      SupermarketId supermarketId,
      CategoryId categoryId) {

    var priceInstructions = priceMapper.toDomain(dto.priceInstructions());

    String ean = detail != null ? detail.ean() : null;
    String brand = detail != null ? detail.brand() : null;
    String origin = detail != null ? detail.origin() : null;
    boolean isBulk = detail != null && detail.isBulk();
    boolean isVariableWeight = detail != null && detail.isVariableWeight();

    String legalName = null;
    String description = null;
    String storageInstructions = null;
    String usageInstructions = null;
    String mandatoryMentions = null;
    String productionVariant = null;
    String dangerMentions = null;
    List<String> supplierNames = List.of();

    if (detail != null && detail.details() != null) {
      var d = detail.details();
      legalName = d.legalName();
      description = d.description();
      storageInstructions = d.storageInstructions();
      usageInstructions = d.usageInstructions();
      mandatoryMentions = d.mandatoryMentions();
      productionVariant = d.productionVariant();
      dangerMentions = d.dangerMentions();
      if (d.suppliers() != null) {
        supplierNames =
            d.suppliers().stream()
                .filter(s -> s != null && s.name() != null)
                .map(MercadonaSupplierDto::name)
                .toList();
      }
    }

    String allergens = null;
    String ingredients = null;
    if (detail != null && detail.nutritionInformation() != null) {
      allergens = detail.nutritionInformation().allergens();
      ingredients = detail.nutritionInformation().ingredients();
    }

    return new UpsertProductCommand(
        dto.id(),
        supermarketId.value(),
        categoryId.value(),
        dto.displayName(),
        legalName,
        description,
        brand,
        ean,
        origin,
        dto.packaging(),
        dto.thumbnail(),
        storageInstructions,
        usageInstructions,
        mandatoryMentions,
        productionVariant,
        dangerMentions,
        allergens,
        ingredients,
        supplierNames,
        dto.badges() != null && dto.badges().isWater(),
        dto.badges() != null && dto.badges().requiresAgeCheck(),
        isBulk,
        isVariableWeight,
        dto.limit(),
        priceInstructions);
  }
}
