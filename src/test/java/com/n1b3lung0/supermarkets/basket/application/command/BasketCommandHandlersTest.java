package com.n1b3lung0.supermarkets.basket.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.n1b3lung0.supermarkets.basket.application.dto.AddBasketItemCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.ClearBasketCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.CreateBasketCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.RemoveBasketItemCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.UpdateBasketItemQuantityCommand;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for all Basket command handlers. */
@ExtendWith(MockitoExtension.class)
class BasketCommandHandlersTest {

  @Mock private BasketRepositoryPort repository;

  @Test
  void createBasket_savesAndReturnsId() {
    var handler = new CreateBasketHandler(repository);
    var id = handler.execute(new CreateBasketCommand("Mi cesta"));
    assertThat(id).isNotNull();
    verify(repository).save(any(Basket.class));
  }

  @Test
  void addItem_basketNotFound_throws() {
    given(repository.findById(any())).willReturn(Optional.empty());
    var handler = new AddBasketItemHandler(repository);
    assertThatThrownBy(
            () -> handler.execute(new AddBasketItemCommand(UUID.randomUUID(), "Leche", 1)))
        .isInstanceOf(BasketNotFoundException.class);
  }

  @Test
  void addItem_savesBasketAndReturnsItemId() {
    var basket = Basket.create("Cesta");
    given(repository.findById(BasketId.of(basket.getId().value()))).willReturn(Optional.of(basket));
    var handler = new AddBasketItemHandler(repository);

    var itemId = handler.execute(new AddBasketItemCommand(basket.getId().value(), "Leche", 2));

    assertThat(itemId).isNotNull();
    verify(repository).save(basket);
  }

  @Test
  void removeItem_basketNotFound_throws() {
    given(repository.findById(any())).willReturn(Optional.empty());
    var handler = new RemoveBasketItemHandler(repository);
    assertThatThrownBy(
            () ->
                handler.execute(new RemoveBasketItemCommand(UUID.randomUUID(), UUID.randomUUID())))
        .isInstanceOf(BasketNotFoundException.class);
  }

  @Test
  void updateQuantity_basketNotFound_throws() {
    given(repository.findById(any())).willReturn(Optional.empty());
    var handler = new UpdateBasketItemQuantityHandler(repository);
    assertThatThrownBy(
            () ->
                handler.execute(
                    new UpdateBasketItemQuantityCommand(UUID.randomUUID(), UUID.randomUUID(), 3)))
        .isInstanceOf(BasketNotFoundException.class);
  }

  @Test
  void clearBasket_clearsAndSaves() {
    var basket = Basket.create("Cesta");
    basket.addItem("Leche", 1);
    given(repository.findById(BasketId.of(basket.getId().value()))).willReturn(Optional.of(basket));
    var handler = new ClearBasketHandler(repository);

    handler.execute(new ClearBasketCommand(basket.getId().value()));

    assertThat(basket.getItems()).isEmpty();
    verify(repository).save(basket);
  }
}
