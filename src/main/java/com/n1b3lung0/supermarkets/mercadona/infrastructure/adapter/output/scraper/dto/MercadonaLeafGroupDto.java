package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Leaf group (level-2) — contains the actual product list. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaLeafGroupDto(
    int id,
    String name,
    int order,
    int layout,
    boolean published,
    @JsonProperty("is_extended") boolean isExtended,
    String image,
    String subtitle,
    @JsonProperty("products") List<MercadonaProductInCategoryDto> products) {}
