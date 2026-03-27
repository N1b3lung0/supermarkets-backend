package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Full product detail from GET /api/products/{id}. Includes fields absent from the categories
 * endpoint: ean, origin, details, isBulk, isVariableWeight, nutritionInformation, photos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaProductDetailDto(
    String id,
    String ean,
    String slug,
    String brand,
    int limit,
    MercadonaBadgesDto badges,
    String origin,
    @JsonProperty("photos") List<MercadonaPhotoDto> photos,
    String status,
    MercadonaDetailsDto details,
    @JsonProperty("is_bulk") boolean isBulk,
    String packaging,
    boolean published,
    @JsonProperty("share_url") String shareUrl,
    String thumbnail,
    @JsonProperty("categories") List<MercadonaProductCategoryRefDto> categories,
    @JsonProperty("display_name") String displayName,
    @JsonProperty("unavailable_from") String unavailableFrom,
    @JsonProperty("is_variable_weight") boolean isVariableWeight,
    @JsonProperty("price_instructions") MercadonaPriceInstructionsDto priceInstructions,
    @JsonProperty("unavailable_weekdays") List<String> unavailableWeekdays,
    @JsonProperty("nutrition_information") MercadonaNutritionInformationDto nutritionInformation) {}
