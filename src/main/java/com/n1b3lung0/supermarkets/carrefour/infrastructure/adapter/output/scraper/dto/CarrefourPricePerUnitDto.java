package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Reference price per unit (e.g. per kg, per litre). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourPricePerUnitDto(BigDecimal value, String unit, String formattedValue) {}
