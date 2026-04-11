package com.n1b3lung0.supermarkets.shared.infrastructure.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;

class InfrastructureExceptionHierarchyTest {

  @Test
  void externalServiceException_shouldExtendInfrastructureException() {
    // given
    var ex = new ExternalServiceException("MercadonaAPI", "timeout");

    // then
    assertThat(ex).isInstanceOf(ExternalServiceException.class);
    assertThat(ex).isInstanceOf(InfrastructureException.class);
    assertThat(ex).isInstanceOf(RuntimeException.class);
    assertThat(ex.getMessage()).contains("MercadonaAPI").contains("timeout");
  }

  @Test
  void externalServiceException_withCause_shouldPreserveCause() {
    // given
    var cause = new RuntimeException("root cause");
    var ex = new ExternalServiceException("SomeService", "detail", cause);

    // then
    assertThat(ex.getCause()).isSameAs(cause);
  }

  @Test
  void domainExceptions_shouldNotExtendInfrastructureException() {
    // given
    var domainEx = new NotFoundException("not found") {
          // anonymous concrete subclass
        };

    // then
    assertThat(domainEx).isNotInstanceOf(InfrastructureException.class);
  }
}
