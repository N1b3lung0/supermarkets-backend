package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — danger mentions (maps to API field {@code details.danger_mentions}). Nullable, max
 * 1000.
 */
public record DangerMentions(String value) {

  public DangerMentions {
    if (value != null && value.length() > 1000) {
      throw new IllegalArgumentException(
          "DangerMentions cannot exceed 1000 characters, got: " + value.length());
    }
  }

  public static DangerMentions of(String value) {
    return new DangerMentions(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
