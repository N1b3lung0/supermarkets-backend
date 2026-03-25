package com.n1b3lung0.supermarkets.supermarket.domain.model;

import com.n1b3lung0.supermarkets.supermarket.domain.event.SupermarketEvent;
import com.n1b3lung0.supermarkets.supermarket.domain.event.SupermarketRegistered;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root for the Supermarket bounded context. Protects all invariants and accumulates
 * domain events.
 */
public class Supermarket {

  private final SupermarketId id;
  private SupermarketName name;
  private SupermarketCountry country;
  private final List<SupermarketEvent> domainEvents = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Factory method — business entry point
  // -------------------------------------------------------------------------

  /**
   * Creates a new Supermarket. Validates all invariants and emits {@link SupermarketRegistered}.
   */
  public static Supermarket create(SupermarketName name, SupermarketCountry country) {
    Objects.requireNonNull(name, "name is required");
    Objects.requireNonNull(country, "country is required");

    var supermarket = new Supermarket(SupermarketId.generate(), name, country);
    supermarket.domainEvents.add(
        new SupermarketRegistered(supermarket.id, supermarket.name, Instant.now()));
    return supermarket;
  }

  // -------------------------------------------------------------------------
  // Reconstitution factory — only for the persistence mapper. No events emitted.
  // -------------------------------------------------------------------------

  /**
   * Reconstitutes a Supermarket from persisted state. No invariants are re-checked and no domain
   * events are emitted. Only the persistence mapper should call this.
   */
  public static Supermarket reconstitute(
      SupermarketId id, SupermarketName name, SupermarketCountry country) {
    return new Supermarket(id, name, country);
  }

  private Supermarket(SupermarketId id, SupermarketName name, SupermarketCountry country) {
    this.id = id;
    this.name = name;
    this.country = country;
  }

  // -------------------------------------------------------------------------
  // Domain behaviour
  // -------------------------------------------------------------------------

  /** Renames the supermarket. */
  public void rename(SupermarketName newName) {
    Objects.requireNonNull(newName, "newName is required");
    this.name = newName;
  }

  // -------------------------------------------------------------------------
  // Domain events
  // -------------------------------------------------------------------------

  /**
   * Returns and clears the accumulated domain events. Must be called after the aggregate is
   * persisted.
   */
  public List<SupermarketEvent> pullDomainEvents() {
    var events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }

  // -------------------------------------------------------------------------
  // Accessors (no setters — state changes only via domain methods)
  // -------------------------------------------------------------------------

  public SupermarketId getId() {
    return id;
  }

  public SupermarketName getName() {
    return name;
  }

  public SupermarketCountry getCountry() {
    return country;
  }
}
