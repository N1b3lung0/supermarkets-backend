package com.n1b3lung0.supermarkets.supermarket.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.n1b3lung0.supermarkets.supermarket.domain.event.SupermarketRegistered;
import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketCountry;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import org.junit.jupiter.api.Test;

class SupermarketTest {

  @Test
  void create_shouldInitializeWithCorrectState() {
    // given
    var name = SupermarketName.of("Mercadona");
    var country = SupermarketCountry.of("ES");

    // when
    var supermarket = Supermarket.create(name, country);

    // then
    assertThat(supermarket.getId()).isNotNull();
    assertThat(supermarket.getName()).isEqualTo(name);
    assertThat(supermarket.getCountry()).isEqualTo(country);
  }

  @Test
  void create_shouldEmitSupermarketRegisteredEvent() {
    // given / when
    var supermarket = SupermarketMother.anySpanish();

    // then
    var events = supermarket.pullDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.getFirst()).isInstanceOf(SupermarketRegistered.class);
    var event = (SupermarketRegistered) events.getFirst();
    assertThat(event.supermarketId()).isEqualTo(supermarket.getId());
  }

  @Test
  void pullDomainEvents_shouldClearEventsAfterPull() {
    // given
    var supermarket = SupermarketMother.anySpanish();

    // when
    supermarket.pullDomainEvents();

    // then
    assertThat(supermarket.pullDomainEvents()).isEmpty();
  }

  @Test
  void rename_shouldUpdateName() {
    // given
    var supermarket = SupermarketMother.anySpanish();
    var newName = SupermarketName.of("SuperMercado");

    // when
    supermarket.rename(newName);

    // then
    assertThat(supermarket.getName()).isEqualTo(newName);
  }

  @Test
  void create_shouldThrowException_whenNameIsNull() {
    // when / then
    assertThatThrownBy(() -> Supermarket.create(null, SupermarketCountry.of("ES")))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void supermarketName_shouldThrowException_whenBlank() {
    assertThatThrownBy(() -> SupermarketName.of("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void supermarketCountry_shouldThrowException_whenInvalidCode() {
    assertThatThrownBy(() -> SupermarketCountry.of("XX"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
