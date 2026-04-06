package com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Paged products response from the DIA catalog API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiaProductPageDto(
    List<DiaProductDto> products, int page, int size, int totalElements, int totalPages) {}
