package com.n1b3lung0.supermarkets.product.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductByIdQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductQueryPort;
import com.n1b3lung0.supermarkets.product.domain.ProductMother;
import com.n1b3lung0.supermarkets.product.domain.exception.ProductNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProductByIdHandlerTest {

  @Mock private ProductQueryPort queryPort;

  private ProductDetailView sampleView(UUID id) {
    return new ProductDetailView(
        id,
        "3400",
        ProductMother.DEFAULT_SUPERMARKET.value(),
        ProductMother.DEFAULT_CATEGORY.value(),
        "Leche Entera",
        null,
        null,
        "Hacendado",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        List.of(),
        false,
        false,
        false,
        false,
        true,
        0,
        Instant.now(),
        Instant.now(),
        null);
  }

  @Test
  void execute_found_shouldReturnView() {
    var id = UUID.randomUUID();
    given(queryPort.findDetailById(any())).willReturn(Optional.of(sampleView(id)));

    var result = new GetProductByIdHandler(queryPort).execute(new GetProductByIdQuery(id));

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(id);
  }

  @Test
  void execute_notFound_shouldThrowProductNotFoundException() {
    given(queryPort.findDetailById(any())).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                new GetProductByIdHandler(queryPort)
                    .execute(new GetProductByIdQuery(UUID.randomUUID())))
        .isInstanceOf(ProductNotFoundException.class);
  }
}
