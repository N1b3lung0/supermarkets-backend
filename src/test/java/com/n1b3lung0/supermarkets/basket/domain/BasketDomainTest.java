package com.n1b3lung0.supermarkets.basket.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.n1b3lung0.supermarkets.basket.domain.event.BasketCreated;
import com.n1b3lung0.supermarkets.basket.domain.event.BasketItemAdded;
import com.n1b3lung0.supermarkets.basket.domain.event.BasketItemRemoved;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketItemNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.exception.DuplicateBasketItemException;
import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;
import org.junit.jupiter.api.Test;

class BasketDomainTest {

  @Test
  void create_emitsBasketCreatedEvent() {
    var basket = Basket.create("Mi cesta");
    var events = basket.pullDomainEvents();
    assertThat(events).hasSize(1).first().isInstanceOf(BasketCreated.class);
    assertThat(((BasketCreated) events.get(0)).name()).isEqualTo("Mi cesta");
  }

  @Test
  void addItem_addsItemAndEmitsEvent() {
    var basket = Basket.create("Cesta");
    basket.pullDomainEvents(); // clear create event

    basket.addItem("Leche 1L", 2);

    assertThat(basket.getItems()).hasSize(1);
    var events = basket.pullDomainEvents();
    assertThat(events).hasSize(1).first().isInstanceOf(BasketItemAdded.class);
  }

  @Test
  void addItem_duplicateProductName_throwsDuplicateBasketItemException() {
    var basket = Basket.create("Cesta");
    basket.addItem("Leche 1L", 1);
    assertThatThrownBy(() -> basket.addItem("leche 1l", 1))
        .isInstanceOf(DuplicateBasketItemException.class);
  }

  @Test
  void removeItem_removesAndEmitsEvent() {
    var basket = Basket.create("Cesta");
    var item = basket.addItem("Aceite 1L", 1);
    basket.pullDomainEvents();

    basket.removeItem(item.getId());

    assertThat(basket.getItems()).isEmpty();
    var events = basket.pullDomainEvents();
    assertThat(events).hasSize(1).first().isInstanceOf(BasketItemRemoved.class);
  }

  @Test
  void removeItem_nonExistent_throwsBasketItemNotFoundException() {
    var basket = Basket.create("Cesta");
    assertThatThrownBy(() -> basket.removeItem(BasketItemId.generate()))
        .isInstanceOf(BasketItemNotFoundException.class);
  }

  @Test
  void updateQuantity_updatesItem() {
    var basket = Basket.create("Cesta");
    var item = basket.addItem("Pan 400g", 1);
    basket.updateItemQuantity(item.getId(), 3);
    assertThat(basket.getItems().get(0).getQuantity()).isEqualTo(3);
  }

  @Test
  void clear_removesAllItemsAndEmitsEvents() {
    var basket = Basket.create("Cesta");
    basket.addItem("Leche", 1);
    basket.addItem("Pan", 2);
    basket.pullDomainEvents();

    basket.clear();

    assertThat(basket.getItems()).isEmpty();
    assertThat(basket.pullDomainEvents()).hasSize(2).allMatch(e -> e instanceof BasketItemRemoved);
  }

  @Test
  void pullDomainEvents_clearsEventsAfterPull() {
    var basket = Basket.create("Cesta");
    basket.pullDomainEvents();
    assertThat(basket.pullDomainEvents()).isEmpty();
  }
}
