package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Paged products response from LIDL's gridboxes endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LidlGridBoxPageDto(
    List<LidlGridBoxDto> gridBoxes, int totalCount, int page, int pageSize) {}
