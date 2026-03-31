package com.n1b3lung0.supermarkets.basket.domain.model;

import com.n1b3lung0.supermarkets.basket.domain.event.BasketCreated;
import com.n1b3lung0.supermarkets.basket.domain.event.BasketEvent;
import com.n1b3lung0.supermarkets.basket.domain.event.BasketItemAdded;
import com.n1b3lung0.supermarkets.basket.domain.event.BasketItemRemoved;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketItemNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.exception.DuplicateBasketItemException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Aggregate Root for a shopping basket. Carries domain events — never persists Spring beans. */
public class Basket {

  private final BasketId id;
  private final String name;
  private final List<BasketItem> items;
  private final Instant createdAt;
  private Instant updatedAt;

  private final List<BasketEvent> domainEvents = new ArrayList<>();

  private Basket(
      BasketId id, String name, List<BasketItem> items, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.items = new ArrayList<>(items);
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // -------------------------------------------------------------------------
  // Factory
  // -------------------------------------------------------------------------

  public static Basket create(String name) {
    Objects.requireNonNull(name, "name is required");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    var now = Instant.now();
    var basket = new Basket(BasketId.generate(), name, List.of(), now, now);
    basket.domainEvents.add(new BasketCreated(basket.id, name));
    return basket;
  }

  /** Reconstitute from persistence — no events emitted. */
  public static Basket reconstitute(
      BasketId id, String name, List<BasketItem> items, Instant createdAt, Instant updatedAt) {
    return new Basket(id, name, items, createdAt, updatedAt);
  }

  // -------------------------------------------------------------------------
  // Business behaviour
  // -------------------------------------------------------------------------

  public BasketItem addItem(String productName, int quantity) {
    boolean duplicate =
        items.stream().anyMatch(i -> i.getProductName().equalsIgnoreCase(productName));
    if (duplicate) {
      throw new DuplicateBasketItemException(productName);
    }

    var item = BasketItem.create(productName, quantity);
    items.add(item);
    touch();
    domainEvents.add(new BasketItemAdded(id, item.getId(), productName, quantity));
    return item;
  }

  public void removeItem(BasketItemId itemId) {
    var item = findItem(itemId);
    items.remove(item);
    touch();
    domainEvents.add(new BasketItemRemoved(id, itemId));
  }

  public void updateItemQuantity(BasketItemId itemId, int quantity) {
    findItem(itemId).updateQuantity(quantity);
    touch();
  }

  public void clear() {
    var removed = new ArrayList<>(items);
    items.clear();
    touch();
    removed.forEach(i -> domainEvents.add(new BasketItemRemoved(id, i.getId())));
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private BasketItem findItem(BasketItemId itemId) {
    return items.stream()
        .filter(i -> i.getId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new BasketItemNotFoundException(itemId.value().toString()));
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public List<BasketEvent> pullDomainEvents() {
    var events = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(events);
  }

  // -------------------------------------------------------------------------
  // Accessors
  // -------------------------------------------------------------------------

  public BasketId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<BasketItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
