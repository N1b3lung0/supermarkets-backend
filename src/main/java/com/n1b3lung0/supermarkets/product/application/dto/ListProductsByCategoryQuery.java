package com.n1b3lung0.supermarkets.product.application.dto;

import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** Query to list products by category (paginated). */
public record ListProductsByCategoryQuery(UUID categoryId, Pageable pageable) {}
