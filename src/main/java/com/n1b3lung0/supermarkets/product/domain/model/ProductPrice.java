package com.n1b3lung0.supermarkets.product.domain.model;

import com.n1b3lung0.supermarkets.product.domain.event.ProductPriceEvent;
import com.n1b3lung0.supermarkets.product.domain.event.ProductPriceRecorded;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root — immutable price snapshot for a Product at a given point in time.
 *
 * <p>Price records are append-only — never updated. Each daily scraper run creates a new entry,
 * enabling full price history and trend analysis.
 */
public class ProductPrice {

  private final ProductPriceId id;
  private final ProductId productId;
  private final PriceInstructions priceInstructions;
  private final Instant recordedAt;

  private final List<ProductPriceEvent> domainEvents = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Factory
  // -------------------------------------------------------------------------

  public static ProductPrice create(ProductId productId, PriceInstructions priceInstructions) {
    Objects.requireNonNull(productId, "productId is required");
    Objects.requireNonNull(priceInstructions, "priceInstructions is required");

    var now = Instant.now();
    var price = new ProductPrice(ProductPriceId.generate(), productId, priceInstructions, now);
    price.domainEvents.add(
        new ProductPriceRecorded(
            price.id,
            price.productId,
            priceInstructions.unitPrice(),
            priceInstructions.bulkPrice(),
            now));
    return price;
  }

  // -------------------------------------------------------------------------
  // Reconstitution factory — persistence only, no events
  // -------------------------------------------------------------------------

  public static ProductPrice reconstitute(
      ProductPriceId id,
      ProductId productId,
      PriceInstructions priceInstructions,
      Instant recordedAt) {
    return new ProductPrice(id, productId, priceInstructions, recordedAt);
  }

  private ProductPrice(
      ProductPriceId id,
      ProductId productId,
      PriceInstructions priceInstructions,
      Instant recordedAt) {
    this.id = id;
    this.productId = productId;
    this.priceInstructions = priceInstructions;
    this.recordedAt = recordedAt;
  }

  // -------------------------------------------------------------------------
  // Domain events
  // -------------------------------------------------------------------------

  public List<ProductPriceEvent> pullDomainEvents() {
    var events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }

  // -------------------------------------------------------------------------
  // Accessors
  // -------------------------------------------------------------------------

  public ProductPriceId getId() {
    return id;
  }

  public ProductId getProductId() {
    return productId;
  }

  public PriceInstructions getPriceInstructions() {
    return priceInstructions;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }
}
