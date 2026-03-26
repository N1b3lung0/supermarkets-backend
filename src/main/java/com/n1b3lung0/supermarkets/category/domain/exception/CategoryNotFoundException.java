package com.n1b3lung0.supermarkets.category.domain.exception;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;

/** Thrown when a requested Category does not exist. Maps to HTTP 404. */
public class CategoryNotFoundException extends NotFoundException {

  public CategoryNotFoundException(CategoryId id) {
    super("Category not found with id: " + id);
  }
}
