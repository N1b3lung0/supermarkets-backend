package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for the product_prices table. Append-only — rows are never updated. */
@Entity
@Table(name = "product_prices")
public class ProductPriceEntity {

  @Id private UUID id;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "bulk_price", precision = 10, scale = 2)
  private BigDecimal bulkPrice;

  @Column(name = "reference_price", precision = 10, scale = 2)
  private BigDecimal referencePrice;

  @Column(name = "reference_format", length = 20)
  private String referenceFormat;

  @Column(name = "size_format", length = 20)
  private String sizeFormat;

  @Column(name = "unit_size")
  private Double unitSize;

  @Column(name = "unit_name", length = 20)
  private String unitName;

  @Column(name = "total_units")
  private Integer totalUnits;

  @Column(name = "pack_size")
  private Integer packSize;

  @Column(name = "iva")
  private Integer iva;

  @Column(name = "tax_percentage", length = 10)
  private String taxPercentage;

  @Column(name = "selling_method", nullable = false)
  private int sellingMethod;

  @Column(name = "is_new", nullable = false)
  private boolean isNew;

  @Column(name = "is_pack", nullable = false)
  private boolean isPack;

  @Column(name = "approx_size", nullable = false)
  private boolean approxSize;

  @Column(name = "price_decreased", nullable = false)
  private boolean priceDecreased;

  @Column(name = "unit_selector", nullable = false)
  private boolean unitSelector;

  @Column(name = "bunch_selector", nullable = false)
  private boolean bunchSelector;

  @Column(name = "previous_unit_price", precision = 10, scale = 2)
  private BigDecimal previousUnitPrice;

  @Column(name = "min_bunch_amount")
  private Double minBunchAmount;

  @Column(name = "increment_bunch_amount")
  private Double incrementBunchAmount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  public ProductPriceEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getProductId() {
    return productId;
  }

  public void setProductId(UUID productId) {
    this.productId = productId;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
  }

  public BigDecimal getBulkPrice() {
    return bulkPrice;
  }

  public void setBulkPrice(BigDecimal bulkPrice) {
    this.bulkPrice = bulkPrice;
  }

  public BigDecimal getReferencePrice() {
    return referencePrice;
  }

  public void setReferencePrice(BigDecimal referencePrice) {
    this.referencePrice = referencePrice;
  }

  public String getReferenceFormat() {
    return referenceFormat;
  }

  public void setReferenceFormat(String referenceFormat) {
    this.referenceFormat = referenceFormat;
  }

  public String getSizeFormat() {
    return sizeFormat;
  }

  public void setSizeFormat(String sizeFormat) {
    this.sizeFormat = sizeFormat;
  }

  public Double getUnitSize() {
    return unitSize;
  }

  public void setUnitSize(Double unitSize) {
    this.unitSize = unitSize;
  }

  public String getUnitName() {
    return unitName;
  }

  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  public Integer getTotalUnits() {
    return totalUnits;
  }

  public void setTotalUnits(Integer totalUnits) {
    this.totalUnits = totalUnits;
  }

  public Integer getPackSize() {
    return packSize;
  }

  public void setPackSize(Integer packSize) {
    this.packSize = packSize;
  }

  public Integer getIva() {
    return iva;
  }

  public void setIva(Integer iva) {
    this.iva = iva;
  }

  public String getTaxPercentage() {
    return taxPercentage;
  }

  public void setTaxPercentage(String taxPercentage) {
    this.taxPercentage = taxPercentage;
  }

  public int getSellingMethod() {
    return sellingMethod;
  }

  public void setSellingMethod(int sellingMethod) {
    this.sellingMethod = sellingMethod;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  public boolean isPack() {
    return isPack;
  }

  public void setPack(boolean isPack) {
    this.isPack = isPack;
  }

  public boolean isApproxSize() {
    return approxSize;
  }

  public void setApproxSize(boolean approxSize) {
    this.approxSize = approxSize;
  }

  public boolean isPriceDecreased() {
    return priceDecreased;
  }

  public void setPriceDecreased(boolean priceDecreased) {
    this.priceDecreased = priceDecreased;
  }

  public boolean isUnitSelector() {
    return unitSelector;
  }

  public void setUnitSelector(boolean unitSelector) {
    this.unitSelector = unitSelector;
  }

  public boolean isBunchSelector() {
    return bunchSelector;
  }

  public void setBunchSelector(boolean bunchSelector) {
    this.bunchSelector = bunchSelector;
  }

  public BigDecimal getPreviousUnitPrice() {
    return previousUnitPrice;
  }

  public void setPreviousUnitPrice(BigDecimal previousUnitPrice) {
    this.previousUnitPrice = previousUnitPrice;
  }

  public Double getMinBunchAmount() {
    return minBunchAmount;
  }

  public void setMinBunchAmount(Double minBunchAmount) {
    this.minBunchAmount = minBunchAmount;
  }

  public Double getIncrementBunchAmount() {
    return incrementBunchAmount;
  }

  public void setIncrementBunchAmount(Double incrementBunchAmount) {
    this.incrementBunchAmount = incrementBunchAmount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }

  public void setRecordedAt(Instant recordedAt) {
    this.recordedAt = recordedAt;
  }
}
