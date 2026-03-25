package com.n1b3lung0.supermarkets.shared.domain.exception;

/**
 * Thrown when an operation conflicts with the current state (e.g. duplicate, optimistic lock). Maps
 * to HTTP 409.
 */
public abstract class ConflictException extends DomainException {

  protected ConflictException(String message) {
    super(message);
  }

  protected ConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
