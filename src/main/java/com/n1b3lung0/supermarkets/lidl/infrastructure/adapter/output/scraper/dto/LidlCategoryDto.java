package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A category slug returned by the LIDL categories endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LidlCategoryDto(String id, String name) {}
