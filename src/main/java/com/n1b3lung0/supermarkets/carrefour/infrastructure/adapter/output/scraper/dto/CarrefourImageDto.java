package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Product image reference. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourImageDto(String url, String format) {}
