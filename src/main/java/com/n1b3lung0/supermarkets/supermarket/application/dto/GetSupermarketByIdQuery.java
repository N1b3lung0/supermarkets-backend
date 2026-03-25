package com.n1b3lung0.supermarkets.supermarket.application.dto;

import java.util.UUID;

/** Query to retrieve a single Supermarket by its identifier. */
public record GetSupermarketByIdQuery(UUID id) {}
