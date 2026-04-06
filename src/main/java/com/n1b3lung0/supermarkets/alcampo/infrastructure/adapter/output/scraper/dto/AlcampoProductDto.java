package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** A single product in the Alcampo search response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlcampoProductDto(
    String code,
    String name,
    AlcampoPriceDto price,
    List<AlcampoCategoryRefDto> categories,
    List<AlcampoImageDto> images) {}
