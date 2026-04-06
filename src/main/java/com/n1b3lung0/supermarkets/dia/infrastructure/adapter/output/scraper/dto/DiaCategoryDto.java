package com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** A category in the DIA catalog with optional children. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiaCategoryDto(String id, String name, List<DiaCategoryDto> children) {}
