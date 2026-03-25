package com.n1b3lung0.supermarkets.supermarket.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.n1b3lung0.supermarkets.supermarket.application.dto.GetSupermarketByIdQuery;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;
import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketQueryPort;
import com.n1b3lung0.supermarkets.supermarket.domain.exception.SupermarketNotFoundException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSupermarketByIdHandlerTest {

  @Mock private SupermarketQueryPort queryPort;

  private GetSupermarketByIdHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GetSupermarketByIdHandler(queryPort);
  }

  @Test
  void execute_shouldReturnDetailView_whenSupermarketExists() {
    // given
    var id = UUID.randomUUID();
    var view = new SupermarketDetailView(id, "Mercadona", "ES", Instant.now());
    given(queryPort.findDetailById(SupermarketId.of(id))).willReturn(Optional.of(view));

    // when
    var result = handler.execute(new GetSupermarketByIdQuery(id));

    // then
    assertThat(result).isEqualTo(view);
  }

  @Test
  void execute_shouldThrowSupermarketNotFoundException_whenNotFound() {
    // given
    var id = UUID.randomUUID();
    given(queryPort.findDetailById(SupermarketId.of(id))).willReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> handler.execute(new GetSupermarketByIdQuery(id)))
        .isInstanceOf(SupermarketNotFoundException.class);
  }
}
