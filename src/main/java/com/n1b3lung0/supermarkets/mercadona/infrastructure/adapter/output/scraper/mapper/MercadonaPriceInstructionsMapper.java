package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper;

import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaPriceInstructionsDto;
import com.n1b3lung0.supermarkets.product.domain.model.PriceInstructions;
import com.n1b3lung0.supermarkets.product.domain.model.SellingMethod;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.math.BigDecimal;

/** Maps {@link MercadonaPriceInstructionsDto} → {@link PriceInstructions} domain object. */
public class MercadonaPriceInstructionsMapper {

  private static final String EUR = "EUR";

  public PriceInstructions toDomain(MercadonaPriceInstructionsDto dto) {
    var unitPrice = parseMoney(dto.unitPrice());
    var bulkPrice = dto.bulkPrice() != null ? parseMoney(dto.bulkPrice()) : null;
    var referencePrice = dto.referencePrice() != null ? parseMoney(dto.referencePrice()) : null;
    var previousUnitPrice =
        dto.previousUnitPrice() != null && !dto.previousUnitPrice().isBlank()
            ? parseMoney(dto.previousUnitPrice().strip())
            : null;

    return new PriceInstructions(
        unitPrice,
        bulkPrice,
        referencePrice,
        dto.referenceFormat(),
        dto.sizeFormat(),
        dto.unitSize(),
        dto.unitName(),
        dto.totalUnits(),
        dto.packSize(),
        dto.iva(),
        dto.taxPercentage(),
        SellingMethod.fromCode(dto.sellingMethod()),
        dto.isNew(),
        dto.isPack(),
        dto.approxSize(),
        dto.priceDecreased(),
        dto.unitSelector(),
        dto.bunchSelector(),
        previousUnitPrice,
        dto.minBunchAmount(),
        dto.incrementBunchAmount());
  }

  private Money parseMoney(String value) {
    return Money.of(new BigDecimal(value.strip()), EUR);
  }
}
