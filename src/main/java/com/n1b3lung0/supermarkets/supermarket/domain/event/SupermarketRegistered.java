package com.n1b3lung0.supermarkets.supermarket.domain.event;

import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import java.time.Instant;

/** Emitted when a new Supermarket is registered in the system. */
public record SupermarketRegistered(
    SupermarketId supermarketId, SupermarketName name, Instant occurredOn)
    implements SupermarketEvent {}
