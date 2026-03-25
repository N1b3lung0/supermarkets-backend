package com.n1b3lung0.supermarkets.supermarket.domain.exception;

import com.n1b3lung0.supermarkets.shared.domain.exception.ConflictException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;

/** Thrown when attempting to register a Supermarket whose name already exists. */
public class DuplicateSupermarketException extends ConflictException {

  public DuplicateSupermarketException(SupermarketName name) {
    super("Supermarket already exists with name: " + name);
  }
}
