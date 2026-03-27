package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Badge flags on a product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaBadgesDto(
    @JsonProperty("is_water") boolean isWater,
    @JsonProperty("requires_age_check") boolean requiresAgeCheck) {}
