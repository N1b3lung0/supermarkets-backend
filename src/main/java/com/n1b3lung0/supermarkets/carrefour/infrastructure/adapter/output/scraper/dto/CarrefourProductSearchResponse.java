package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Paginated product search response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourProductSearchResponse(
    CarrefourPaginationDto pagination, List<CarrefourProductDto> products) {}
