package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Supplier entry inside product details. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaSupplierDto(String name) {}
