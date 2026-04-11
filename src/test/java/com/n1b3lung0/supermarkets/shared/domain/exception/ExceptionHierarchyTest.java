package com.n1b3lung0.supermarkets.shared.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExceptionHierarchyTest {

  // --- Concrete stubs used only inside this test class ---

  static class SomeNotFoundException extends NotFoundException {
    SomeNotFoundException() {
      super("not found");
    }
  }

  static class SomeBusinessRuleException extends BusinessRuleViolationException {
    SomeBusinessRuleException() {
      super("rule violated");
    }
  }

  static class SomeConflictException extends ConflictException {
    SomeConflictException() {
      super("conflict");
    }
  }

  static class SomeUnauthorizedException extends UnauthorizedException {
    SomeUnauthorizedException() {
      super("unauthorized");
    }
  }

  // --- Tests ---

  @Test
  void notFoundException_shouldExtendDomainException() {
    // given
    var ex = new SomeNotFoundException();

    // then
    assertThat(ex).isInstanceOf(NotFoundException.class);
    assertThat(ex).isInstanceOf(DomainException.class);
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }

  @Test
  void businessRuleViolationException_shouldExtendDomainException() {
    // given
    var ex = new SomeBusinessRuleException();

    // then
    assertThat(ex).isInstanceOf(BusinessRuleViolationException.class);
    assertThat(ex).isInstanceOf(DomainException.class);
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }

  @Test
  void conflictException_shouldExtendDomainException() {
    // given
    var ex = new SomeConflictException();

    // then
    assertThat(ex).isInstanceOf(ConflictException.class);
    assertThat(ex).isInstanceOf(DomainException.class);
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }

  @Test
  void unauthorizedException_shouldExtendDomainException() {
    // given
    var ex = new SomeUnauthorizedException();

    // then
    assertThat(ex).isInstanceOf(UnauthorizedException.class);
    assertThat(ex).isInstanceOf(DomainException.class);
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }
}
