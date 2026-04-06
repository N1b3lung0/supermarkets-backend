package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Spring-style paged response for ALDI products endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AldiProductPageDto(
    List<AldiProductDto> content, int totalElements, int totalPages, int number, int size) {}
