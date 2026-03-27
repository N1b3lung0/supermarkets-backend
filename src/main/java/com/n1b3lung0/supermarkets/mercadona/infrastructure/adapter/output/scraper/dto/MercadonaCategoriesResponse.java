package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Deserializes the top-level response from GET /api/categories/ */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaCategoriesResponse(
    int count, @JsonProperty("results") List<MercadonaTopCategoryDto> results) {}
