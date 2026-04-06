package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** A category node — can be TOP (no parent) or SUB/LEAF (has parent). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourCategoryNodeDto(
    String uid, String localizedTitle, String url, List<CarrefourCategoryNodeDto> children) {}
