package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Root categories response from Alcampo API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlcampoCategoriesResponse(List<AlcampoCategoryNodeDto> subcategories) {}
