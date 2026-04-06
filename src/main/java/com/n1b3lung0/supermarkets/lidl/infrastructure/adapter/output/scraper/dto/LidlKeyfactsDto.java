package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Extra product facts (brand, unit price label) in the LIDL grid box. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LidlKeyfactsDto(String brand) {}
