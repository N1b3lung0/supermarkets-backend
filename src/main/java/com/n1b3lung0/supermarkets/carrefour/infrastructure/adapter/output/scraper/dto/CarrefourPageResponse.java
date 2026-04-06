package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Root response from GET /api/2.0/page?path=/supermercado */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourPageResponse(List<CarrefourComponentDto> components) {}
