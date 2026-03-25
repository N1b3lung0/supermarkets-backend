package com.n1b3lung0.supermarkets.shared.domain.exception;

/** Thrown when the caller is not authorized to perform an operation. Maps to HTTP 403. */
public abstract class UnauthorizedException extends DomainException {

  protected UnauthorizedException(String message) {
    super(message);
  }
}
