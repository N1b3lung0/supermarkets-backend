package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** A single product returned inside the ALDI products paged response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AldiProductDto(
    String id,
    String ean,
    String name,
    String brand,
    BigDecimal price,
    BigDecimal pricePerUnit,
    String unitLabel,
    String currency,
    String imageUrl,
    String categoryId,
    boolean available) {}
