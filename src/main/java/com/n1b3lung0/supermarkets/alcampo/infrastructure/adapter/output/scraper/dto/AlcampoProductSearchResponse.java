package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Paged search response from the Alcampo products API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlcampoProductSearchResponse(
    AlcampoPaginationDto pagination, List<AlcampoProductDto> products) {}
