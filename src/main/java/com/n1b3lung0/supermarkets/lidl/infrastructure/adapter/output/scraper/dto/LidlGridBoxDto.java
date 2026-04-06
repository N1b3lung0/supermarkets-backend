package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A single product in the LIDL gridBoxes response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LidlGridBoxDto(
    String id,
    String fullTitle,
    LidlPriceDto price,
    String image,
    String category,
    LidlKeyfactsDto keyfacts) {}
