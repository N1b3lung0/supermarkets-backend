package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * price_instructions block. All money fields are JSON strings (decimal). iva is nullable (null for
 * fresh produce). previousUnitPrice may contain leading whitespace — callers must strip() before
 * parsing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadonaPriceInstructionsDto(
    Integer iva,
    @JsonProperty("is_new") boolean isNew,
    @JsonProperty("is_pack") boolean isPack,
    @JsonProperty("pack_size") Integer packSize,
    @JsonProperty("unit_name") String unitName,
    @JsonProperty("unit_size") Double unitSize,
    @JsonProperty("bulk_price") String bulkPrice,
    @JsonProperty("unit_price") String unitPrice,
    @JsonProperty("approx_size") boolean approxSize,
    @JsonProperty("size_format") String sizeFormat,
    @JsonProperty("total_units") Integer totalUnits,
    @JsonProperty("unit_selector") boolean unitSelector,
    @JsonProperty("bunch_selector") boolean bunchSelector,
    @JsonProperty("drained_weight") String drainedWeight,
    @JsonProperty("selling_method") int sellingMethod,
    @JsonProperty("tax_percentage") String taxPercentage,
    @JsonProperty("price_decreased") boolean priceDecreased,
    @JsonProperty("reference_price") String referencePrice,
    @JsonProperty("min_bunch_amount") Double minBunchAmount,
    @JsonProperty("reference_format") String referenceFormat,
    @JsonProperty("previous_unit_price") String previousUnitPrice,
    @JsonProperty("increment_bunch_amount") Double incrementBunchAmount) {}
