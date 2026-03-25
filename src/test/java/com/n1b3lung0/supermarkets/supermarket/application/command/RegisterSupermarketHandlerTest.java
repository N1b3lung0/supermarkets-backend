package com.n1b3lung0.supermarkets.supermarket.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.n1b3lung0.supermarkets.supermarket.application.dto.RegisterSupermarketCommand;
import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketRepositoryPort;
import com.n1b3lung0.supermarkets.supermarket.domain.exception.DuplicateSupermarketException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterSupermarketHandlerTest {

  @Mock private SupermarketRepositoryPort repository;

  private RegisterSupermarketHandler handler;

  @BeforeEach
  void setUp() {
    handler = new RegisterSupermarketHandler(repository);
  }

  @Test
  void execute_shouldReturnNewId_whenNameIsUnique() {
    // given
    given(repository.existsByName(any(SupermarketName.class))).willReturn(false);
    var command = new RegisterSupermarketCommand("Mercadona", "ES");

    // when
    SupermarketId id = handler.execute(command);

    // then
    assertThat(id).isNotNull();
    then(repository).should().save(any());
  }

  @Test
  void execute_shouldThrowDuplicateSupermarketException_whenNameAlreadyExists() {
    // given
    given(repository.existsByName(any(SupermarketName.class))).willReturn(true);
    var command = new RegisterSupermarketCommand("Mercadona", "ES");

    // when / then
    assertThatThrownBy(() -> handler.execute(command))
        .isInstanceOf(DuplicateSupermarketException.class);
    then(repository).shouldHaveNoMoreInteractions();
  }

  @Test
  void execute_shouldThrowNullPointerException_whenCommandIsNull() {
    assertThatThrownBy(() -> handler.execute(null)).isInstanceOf(NullPointerException.class);
  }
}
