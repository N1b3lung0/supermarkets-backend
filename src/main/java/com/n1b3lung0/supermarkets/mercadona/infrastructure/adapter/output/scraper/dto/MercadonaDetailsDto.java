package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Product detail block from GET /api/products/{id}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaDetailsDto(
    String brand,
    String origin,
    @JsonProperty("suppliers") List<MercadonaSupplierDto> suppliers,
    @JsonProperty("legal_name") String legalName,
    String description,
    @JsonProperty("danger_mentions") String dangerMentions,
    @JsonProperty("mandatory_mentions") String mandatoryMentions,
    @JsonProperty("production_variant") String productionVariant,
    @JsonProperty("usage_instructions") String usageInstructions,
    @JsonProperty("storage_instructions") String storageInstructions) {}
