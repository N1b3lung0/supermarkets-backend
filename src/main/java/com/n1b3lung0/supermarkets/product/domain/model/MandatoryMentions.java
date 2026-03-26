package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Value Object — mandatory mentions (maps to API field {@code details.mandatory_mentions}).
 * Nullable, max 1000.
 */
public record MandatoryMentions(String value) {

  public MandatoryMentions {
    if (value != null && value.length() > 1000) {
      throw new IllegalArgumentException(
          "MandatoryMentions cannot exceed 1000 characters, got: " + value.length());
    }
  }

  public static MandatoryMentions of(String value) {
    return new MandatoryMentions(value);
  }

  @Override
  public String toString() {
    return value != null ? value : "";
  }
}
