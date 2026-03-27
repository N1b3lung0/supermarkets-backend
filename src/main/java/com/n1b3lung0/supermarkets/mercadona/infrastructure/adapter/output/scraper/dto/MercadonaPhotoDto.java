package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Photo entry from GET /api/products/{id}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaPhotoDto(String zoom, String regular, String thumbnail, int perspective) {}
