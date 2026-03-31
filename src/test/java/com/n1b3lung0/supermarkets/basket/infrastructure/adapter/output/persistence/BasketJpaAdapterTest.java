package com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.mapper.BasketPersistenceMapper;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.repository.SpringBasketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Integration test for BasketJpaAdapter using Testcontainers PostgreSQL. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BasketJpaAdapterTest extends PostgresIntegrationTest {

  @Autowired private SpringBasketRepository springRepository;
  @Autowired private BasketPersistenceMapper mapper;

  private BasketJpaAdapter adapter() {
    return new BasketJpaAdapter(springRepository, mapper);
  }

  @Test
  void save_andFindById_shouldRoundtrip() {
    var adapter = adapter();
    var basket = Basket.create("Lista de la compra");

    adapter.save(basket);
    var found = adapter.findById(basket.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Lista de la compra");
    assertThat(found.get().getItems()).isEmpty();
  }

  @Test
  void addItem_persistsAndReloads() {
    var adapter = adapter();
    var basket = Basket.create("Cesta con items");
    basket.addItem("Leche entera 1L", 2);
    basket.addItem("Aceite oliva 1L", 1);

    adapter.save(basket);
    var found = adapter.findById(basket.getId()).orElseThrow();

    assertThat(found.getItems()).hasSize(2);
    assertThat(found.getItems())
        .extracting(i -> i.getProductName())
        .containsExactlyInAnyOrder("Leche entera 1L", "Aceite oliva 1L");
  }

  @Test
  void removeItem_updatesPersistedBasket() {
    var adapter = adapter();
    var basket = Basket.create("Cesta");
    var item = basket.addItem("Pan 400g", 1);
    adapter.save(basket);

    var reloaded = adapter.findById(basket.getId()).orElseThrow();
    reloaded.removeItem(item.getId());
    adapter.save(reloaded);

    var afterRemove = adapter.findById(basket.getId()).orElseThrow();
    assertThat(afterRemove.getItems()).isEmpty();
  }

  @Test
  void findById_nonExistent_returnsEmpty() {
    assertThat(adapter().findById(BasketId.generate())).isEmpty();
  }
}
