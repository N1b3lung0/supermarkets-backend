package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Level-1 subcategory as it appears inside a top-level category (no products at this level). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaLevel1CategoryDto(
    int id,
    String name,
    int order,
    int layout,
    boolean published,
    @JsonProperty("is_extended") boolean isExtended) {}
