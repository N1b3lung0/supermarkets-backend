package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Unit price of a Carrefour product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrefourPriceDto(BigDecimal value, String currencyIso, String formattedValue) {}
