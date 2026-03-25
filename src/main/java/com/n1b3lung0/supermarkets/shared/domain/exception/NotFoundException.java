package com.n1b3lung0.supermarkets.shared.domain.exception;

/** Thrown when a requested resource does not exist. Maps to HTTP 404. */
public abstract class NotFoundException extends DomainException {

  protected NotFoundException(String message) {
    super(message);
  }
}
