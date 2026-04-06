package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Price object in an Alcampo product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlcampoPriceDto(BigDecimal value, String currencyIso) {}
