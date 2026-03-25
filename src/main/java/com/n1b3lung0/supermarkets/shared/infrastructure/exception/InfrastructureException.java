package com.n1b3lung0.supermarkets.shared.infrastructure.exception;

/** Base class for infrastructure-layer exceptions. */
public abstract class InfrastructureException extends RuntimeException {

  protected InfrastructureException(String message) {
    super(message);
  }

  protected InfrastructureException(String message, Throwable cause) {
    super(message, cause);
  }
}
