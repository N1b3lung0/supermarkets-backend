package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Category reference embedded in a product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourCategoryRefDto(String code, String name) {}
