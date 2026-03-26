package com.n1b3lung0.supermarkets.shared.domain.model;

import com.n1b3lung0.supermarkets.shared.domain.exception.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Shared Value Object representing a monetary amount with currency. Amount must be non-negative.
 * Currency must be a 3-letter ISO-4217 code.
 */
public record Money(BigDecimal amount, String currency) {

  public Money {
    Objects.requireNonNull(amount, "Money amount is required");
    Objects.requireNonNull(currency, "Money currency is required");
    if (currency.length() != 3) {
      throw new IllegalArgumentException(
          "Currency must be a 3-letter ISO-4217 code, got: " + currency);
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Money amount cannot be negative, got: " + amount);
    }
  }

  public static Money of(BigDecimal amount, String currency) {
    return new Money(amount, currency);
  }

  public static Money ofEur(BigDecimal amount) {
    return new Money(amount, "EUR");
  }

  public static Money ofEur(String amount) {
    return ofEur(new BigDecimal(amount));
  }

  /**
   * Adds another {@link Money} amount. Both must share the same currency.
   *
   * @throws CurrencyMismatchException if the currencies differ
   */
  public Money add(Money other) {
    Objects.requireNonNull(other, "other money is required");
    if (!this.currency.equals(other.currency)) {
      throw new CurrencyMismatchException(this.currency, other.currency);
    }
    return new Money(this.amount.add(other.amount), this.currency);
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + currency;
  }

  /** Thrown when two {@link Money} values with different currencies are combined. */
  public static final class CurrencyMismatchException extends BusinessRuleViolationException {
    public CurrencyMismatchException(String expected, String actual) {
      super("Currency mismatch: cannot combine " + expected + " with " + actual);
    }
  }
}
