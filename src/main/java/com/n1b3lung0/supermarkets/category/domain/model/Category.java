package com.n1b3lung0.supermarkets.category.domain.model;

import com.n1b3lung0.supermarkets.category.domain.event.CategoryEvent;
import com.n1b3lung0.supermarkets.category.domain.event.CategoryRegistered;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root for the Category bounded context.
 *
 * <p>A Category belongs to exactly one Supermarket and occupies one level in a 3-level tree: Top →
 * Sub → Leaf. Products will be attached to Leaf categories in Phase 3.
 */
public class Category {

  private final CategoryId id;
  private CategoryName name;
  private final ExternalCategoryId externalId;
  private final SupermarketId supermarketId;
  private final CategoryLevel level;
  private CategoryOrder order;
  private final List<CategoryEvent> domainEvents = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Factory method — business entry point
  // -------------------------------------------------------------------------

  public static Category create(
      CategoryName name,
      ExternalCategoryId externalId,
      SupermarketId supermarketId,
      CategoryLevel level,
      CategoryOrder order) {
    Objects.requireNonNull(name, "name is required");
    Objects.requireNonNull(externalId, "externalId is required");
    Objects.requireNonNull(supermarketId, "supermarketId is required");
    Objects.requireNonNull(level, "level is required");
    Objects.requireNonNull(order, "order is required");

    var category =
        new Category(CategoryId.generate(), name, externalId, supermarketId, level, order);
    category.domainEvents.add(
        new CategoryRegistered(category.id, category.name, category.supermarketId, Instant.now()));
    return category;
  }

  // -------------------------------------------------------------------------
  // Reconstitution factory — persistence mapper only, no events
  // -------------------------------------------------------------------------

  public static Category reconstitute(
      CategoryId id,
      CategoryName name,
      ExternalCategoryId externalId,
      SupermarketId supermarketId,
      CategoryLevel level,
      CategoryOrder order) {
    return new Category(id, name, externalId, supermarketId, level, order);
  }

  private Category(
      CategoryId id,
      CategoryName name,
      ExternalCategoryId externalId,
      SupermarketId supermarketId,
      CategoryLevel level,
      CategoryOrder order) {
    this.id = id;
    this.name = name;
    this.externalId = externalId;
    this.supermarketId = supermarketId;
    this.level = level;
    this.order = order;
  }

  // -------------------------------------------------------------------------
  // Domain behaviour
  // -------------------------------------------------------------------------

  /** Renames the category (e.g. after a scraper detects a name change). */
  public void rename(CategoryName newName) {
    Objects.requireNonNull(newName, "newName is required");
    this.name = newName;
  }

  /** Updates the display order. */
  public void reorder(CategoryOrder newOrder) {
    Objects.requireNonNull(newOrder, "newOrder is required");
    this.order = newOrder;
  }

  // -------------------------------------------------------------------------
  // Domain events
  // -------------------------------------------------------------------------

  public List<CategoryEvent> pullDomainEvents() {
    var events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }

  // -------------------------------------------------------------------------
  // Accessors
  // -------------------------------------------------------------------------

  public CategoryId getId() {
    return id;
  }

  public CategoryName getName() {
    return name;
  }

  public ExternalCategoryId getExternalId() {
    return externalId;
  }

  public SupermarketId getSupermarketId() {
    return supermarketId;
  }

  public CategoryLevel getLevel() {
    return level;
  }

  public CategoryOrder getOrder() {
    return order;
  }
}
