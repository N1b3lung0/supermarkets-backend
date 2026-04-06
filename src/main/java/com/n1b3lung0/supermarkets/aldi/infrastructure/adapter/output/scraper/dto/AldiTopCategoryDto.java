package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Top-level category returned by GET /api/front/v1/categories. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AldiTopCategoryDto(String id, String name, List<AldiSubCategoryDto> subcategories) {}
