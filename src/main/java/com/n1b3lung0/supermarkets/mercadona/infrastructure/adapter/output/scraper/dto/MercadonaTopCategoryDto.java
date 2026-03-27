package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Top-level category (level 0) from GET /api/categories/ */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaTopCategoryDto(
    int id,
    String name,
    int order,
    @JsonProperty("is_extended") boolean isExtended,
    @JsonProperty("categories") List<MercadonaLevel1CategoryDto> categories) {}
