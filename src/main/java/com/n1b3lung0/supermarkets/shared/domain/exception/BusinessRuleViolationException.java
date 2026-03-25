package com.n1b3lung0.supermarkets.shared.domain.exception;

/** Thrown when an operation violates a domain business rule. Maps to HTTP 422. */
public abstract class BusinessRuleViolationException extends DomainException {

  protected BusinessRuleViolationException(String message) {
    super(message);
  }
}
