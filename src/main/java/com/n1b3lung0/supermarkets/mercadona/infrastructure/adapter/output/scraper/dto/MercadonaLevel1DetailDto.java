package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Full detail of a level-1 subcategory from GET /api/categories/{id} — contains leaf groups. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaLevel1DetailDto(
    int id,
    String name,
    int order,
    int layout,
    boolean published,
    @JsonProperty("is_extended") boolean isExtended,
    @JsonProperty("categories") List<MercadonaLeafGroupDto> categories) {}
