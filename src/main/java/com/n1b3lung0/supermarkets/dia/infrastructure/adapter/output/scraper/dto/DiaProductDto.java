package com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** A single product in the DIA catalog products response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiaProductDto(
    String id,
    String name,
    String brand,
    BigDecimal price,
    String currency,
    String imageUrl,
    String categoryId) {}
