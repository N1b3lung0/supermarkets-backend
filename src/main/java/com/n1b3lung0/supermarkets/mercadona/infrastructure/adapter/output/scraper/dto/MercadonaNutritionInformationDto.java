package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Nutrition block from GET /api/products/{id}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaNutritionInformationDto(String allergens, String ingredients) {}
