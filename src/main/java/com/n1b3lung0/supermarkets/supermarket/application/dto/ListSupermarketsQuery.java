package com.n1b3lung0.supermarkets.supermarket.application.dto;

import org.springframework.data.domain.Pageable;

/** Query to retrieve a paginated list of supermarkets. */
public record ListSupermarketsQuery(Pageable pageable) {}
