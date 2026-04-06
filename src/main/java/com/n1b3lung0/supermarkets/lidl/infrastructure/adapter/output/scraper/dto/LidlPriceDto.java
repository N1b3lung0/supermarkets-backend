package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Price object inside a LIDL grid box product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LidlPriceDto(BigDecimal price, String currency) {}
