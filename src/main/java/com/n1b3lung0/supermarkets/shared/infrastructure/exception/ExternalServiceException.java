package com.n1b3lung0.supermarkets.shared.infrastructure.exception;

/** Thrown when a call to an external service (scraper API, third-party) fails. Maps to HTTP 502. */
public class ExternalServiceException extends InfrastructureException {

  public ExternalServiceException(String serviceName, String detail) {
    super("External service '%s' failed: %s".formatted(serviceName, detail));
  }

  public ExternalServiceException(String serviceName, String detail, Throwable cause) {
    super("External service '%s' failed: %s".formatted(serviceName, detail), cause);
  }
}
