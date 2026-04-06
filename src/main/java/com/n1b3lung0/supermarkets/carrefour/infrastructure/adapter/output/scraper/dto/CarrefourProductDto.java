package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** A single Carrefour product from the search endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourProductDto(
    String code,
    String name,
    String url,
    CarrefourPriceDto price,
    CarrefourPricePerUnitDto pricePerUnit,
    List<CarrefourCategoryRefDto> categories,
    List<CarrefourImageDto> images) {}
