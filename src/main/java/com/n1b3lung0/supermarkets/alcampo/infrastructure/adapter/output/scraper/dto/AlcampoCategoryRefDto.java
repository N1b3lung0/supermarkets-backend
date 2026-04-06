package com.n1b3lung0.supermarkets.alcampo.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A category reference on an Alcampo product. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlcampoCategoryRefDto(String code) {}
