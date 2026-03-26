package com.n1b3lung0.supermarkets.product.application.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.n1b3lung0.supermarkets.product.application.dto.DeactivateProductCommand;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.ProductMother;
import com.n1b3lung0.supermarkets.product.domain.exception.ProductNotFoundException;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeactivateProductHandlerTest {

  @Mock private ProductRepositoryPort repository;

  @Test
  void execute_activeProduct_shouldDeactivateAndSave() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    product.pullDomainEvents();
    var id = product.getId();
    given(repository.findById(id)).willReturn(Optional.of(product));

    new DeactivateProductHandler(repository).execute(new DeactivateProductCommand(id));

    verify(repository, times(1)).save(product);
  }

  @Test
  void execute_notFound_shouldThrow() {
    var id = ProductId.generate();
    given(repository.findById(id)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                new DeactivateProductHandler(repository).execute(new DeactivateProductCommand(id)))
        .isInstanceOf(ProductNotFoundException.class);
  }

  @Test
  void execute_alreadyInactive_shouldNotSave() {
    var product =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    product.deactivate();
    product.pullDomainEvents(); // clear events
    var id = product.getId();
    given(repository.findById(id)).willReturn(Optional.of(product));

    new DeactivateProductHandler(repository).execute(new DeactivateProductCommand(id));

    verify(repository, never()).save(any());
  }
}
