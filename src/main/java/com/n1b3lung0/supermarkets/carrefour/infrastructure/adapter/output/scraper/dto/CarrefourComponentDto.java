package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A single component in the Carrefour page response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourComponentDto(String uid, CarrefourNavigationNodeDto navigationNode) {}
