package com.n1b3lung0.supermarkets.shared.domain.model;

import java.util.List;

/** Generic paginated response record. Framework-agnostic structure exposed via REST. */
public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {}
