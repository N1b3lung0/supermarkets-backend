package com.n1b3lung0.supermarkets.product.domain.model;

/**
 * Enum — maps the {@code selling_method} integer from the Mercadona API.
 *
 * <ul>
 *   <li>{@code 0} → sold by unit (e.g. a tin of tuna)
 *   <li>{@code 2} → sold by weight (e.g. fruit, deli meats)
 * </ul>
 */
public enum SellingMethod {
  UNIT(0),
  WEIGHT(2);

  private final int code;

  SellingMethod(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }

  /**
   * Converts the API integer code to its enum constant. Defaults to {@link #UNIT} for unknown codes
   * to stay resilient against future API changes.
   */
  public static SellingMethod fromCode(int code) {
    for (SellingMethod sm : values()) {
      if (sm.code == code) {
        return sm;
      }
    }
    return UNIT;
  }
}
