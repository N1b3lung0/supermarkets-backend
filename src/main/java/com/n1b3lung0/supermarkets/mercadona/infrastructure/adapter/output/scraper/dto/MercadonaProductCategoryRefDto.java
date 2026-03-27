package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Category ancestor reference on a product (only level-0 is present in category responses). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaProductCategoryRefDto(int id, String name, int level, int order) {}
