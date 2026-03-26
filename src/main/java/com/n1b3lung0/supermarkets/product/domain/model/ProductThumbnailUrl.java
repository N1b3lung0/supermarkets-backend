package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — URL of the product thumbnail image (maps to API field {@code thumbnail}).
 * Nullable, max 1000 characters.
 */
public record ProductThumbnailUrl(String value) {

  public ProductThumbnailUrl {
    if (value != null && value.length() > 1000) {
      throw new IllegalArgumentException(
          "ProductThumbnailUrl cannot exceed 1000 characters, got: " + value.length());
    }
  }

  public static ProductThumbnailUrl of(String value) {
    return new ProductThumbnailUrl(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
