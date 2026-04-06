package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Sub-category node inside a top-level ALDI category. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AldiSubCategoryDto(String id, String name) {}
