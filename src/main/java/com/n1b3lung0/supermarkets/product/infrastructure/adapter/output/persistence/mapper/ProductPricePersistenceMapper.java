package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import com.n1b3lung0.supermarkets.product.domain.model.PriceInstructions;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.product.domain.model.ProductPrice;
import com.n1b3lung0.supermarkets.product.domain.model.ProductPriceId;
import com.n1b3lung0.supermarkets.product.domain.model.SellingMethod;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity.ProductPriceEntity;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;

/** Manual mapper: ProductPriceEntity ↔ ProductPrice domain object. */
public class ProductPricePersistenceMapper {

  // -------------------------------------------------------------------------
  // Domain → Entity
  // -------------------------------------------------------------------------

  public ProductPriceEntity toEntity(ProductPrice price) {
    var pi = price.getPriceInstructions();
    var currency = pi.unitPrice().currency();

    var entity = new ProductPriceEntity();
    entity.setId(price.getId().value());
    entity.setProductId(price.getProductId().value());
    entity.setUnitPrice(pi.unitPrice().amount());
    entity.setBulkPrice(pi.bulkPrice() != null ? pi.bulkPrice().amount() : null);
    entity.setReferencePrice(pi.referencePrice() != null ? pi.referencePrice().amount() : null);
    entity.setReferenceFormat(pi.referenceFormat());
    entity.setSizeFormat(pi.sizeFormat());
    entity.setUnitSize(pi.unitSize());
    entity.setUnitName(pi.unitName());
    entity.setTotalUnits(pi.totalUnits());
    entity.setPackSize(pi.packSize());
    entity.setIva(pi.iva());
    entity.setTaxPercentage(pi.taxPercentage());
    entity.setSellingMethod(pi.sellingMethod().code());
    entity.setNew(pi.isNew());
    entity.setPack(pi.isPack());
    entity.setApproxSize(pi.approxSize());
    entity.setPriceDecreased(pi.priceDecreased());
    entity.setUnitSelector(pi.unitSelector());
    entity.setBunchSelector(pi.bunchSelector());
    entity.setPreviousUnitPrice(
        pi.previousUnitPrice() != null ? pi.previousUnitPrice().amount() : null);
    entity.setMinBunchAmount(pi.minBunchAmount());
    entity.setIncrementBunchAmount(pi.incrementBunchAmount());
    entity.setCurrency(currency);
    entity.setRecordedAt(price.getRecordedAt());
    return entity;
  }

  // -------------------------------------------------------------------------
  // Entity → Domain
  // -------------------------------------------------------------------------

  public ProductPrice toDomain(ProductPriceEntity entity) {
    var currency = entity.getCurrency() != null ? entity.getCurrency() : "EUR";
    var pi =
        new PriceInstructions(
            Money.of(entity.getUnitPrice(), currency),
            entity.getBulkPrice() != null ? Money.of(entity.getBulkPrice(), currency) : null,
            entity.getReferencePrice() != null
                ? Money.of(entity.getReferencePrice(), currency)
                : null,
            entity.getReferenceFormat(),
            entity.getSizeFormat(),
            entity.getUnitSize(),
            entity.getUnitName(),
            entity.getTotalUnits(),
            entity.getPackSize(),
            entity.getIva(),
            entity.getTaxPercentage(),
            SellingMethod.fromCode(entity.getSellingMethod()),
            entity.isNew(),
            entity.isPack(),
            entity.isApproxSize(),
            entity.isPriceDecreased(),
            entity.isUnitSelector(),
            entity.isBunchSelector(),
            entity.getPreviousUnitPrice() != null
                ? Money.of(entity.getPreviousUnitPrice(), currency)
                : null,
            entity.getMinBunchAmount(),
            entity.getIncrementBunchAmount());

    return ProductPrice.reconstitute(
        ProductPriceId.of(entity.getId()),
        ProductId.of(entity.getProductId()),
        pi,
        entity.getRecordedAt());
  }

  // -------------------------------------------------------------------------
  // Entity → View
  // -------------------------------------------------------------------------

  public ProductPriceView toView(ProductPriceEntity entity) {
    var currency = entity.getCurrency() != null ? entity.getCurrency() : "EUR";
    return new ProductPriceView(
        entity.getId(),
        entity.getProductId(),
        Money.of(entity.getUnitPrice(), currency),
        entity.getBulkPrice() != null ? Money.of(entity.getBulkPrice(), currency) : null,
        entity.getReferencePrice() != null ? Money.of(entity.getReferencePrice(), currency) : null,
        entity.getReferenceFormat(),
        entity.getIva(),
        entity.isPriceDecreased(),
        entity.getPreviousUnitPrice() != null
            ? Money.of(entity.getPreviousUnitPrice(), currency)
            : null,
        entity.getRecordedAt());
  }
}
