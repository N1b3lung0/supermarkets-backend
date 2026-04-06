package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Pagination metadata in the Alcampo products search response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlcampoPaginationDto(
    int currentPage, int pageSize, int totalPages, int totalResults) {}
