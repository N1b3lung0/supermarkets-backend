package com.n1b3lung0.supermarkets.shared.domain.exception;

/** Base class for all domain exceptions. Pure Java — no framework dependencies. */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
