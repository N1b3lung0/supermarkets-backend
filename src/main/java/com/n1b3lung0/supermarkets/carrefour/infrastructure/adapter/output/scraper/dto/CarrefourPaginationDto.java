package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Pagination metadata inside the Carrefour products response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourPaginationDto(
    int currentPage, int pageSize, int totalPages, int totalResults) {}
