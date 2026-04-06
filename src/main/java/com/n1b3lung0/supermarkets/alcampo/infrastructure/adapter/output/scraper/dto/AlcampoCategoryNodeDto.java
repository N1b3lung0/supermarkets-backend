package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** A category node in the Alcampo category tree. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlcampoCategoryNodeDto(
    String id, String name, List<AlcampoCategoryNodeDto> subcategories) {}
