package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Product as returned inside a leaf-group categories response. Fields absent from this endpoint:
 * ean, origin, details, nutritionInformation, isBulk, isVariableWeight.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaProductInCategoryDto(
    String id,
    String slug,
    int limit,
    MercadonaBadgesDto badges,
    String status,
    String packaging,
    boolean published,
    @JsonProperty("share_url") String shareUrl,
    String thumbnail,
    @JsonProperty("categories") List<MercadonaProductCategoryRefDto> categories,
    @JsonProperty("display_name") String displayName,
    @JsonProperty("unavailable_from") String unavailableFrom,
    @JsonProperty("price_instructions") MercadonaPriceInstructionsDto priceInstructions,
    @JsonProperty("unavailable_weekdays") List<String> unavailableWeekdays) {}
