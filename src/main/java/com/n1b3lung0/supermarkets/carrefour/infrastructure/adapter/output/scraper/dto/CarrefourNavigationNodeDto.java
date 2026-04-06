package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Navigation node containing top-level category children. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourNavigationNodeDto(List<CarrefourCategoryNodeDto> children) {}
