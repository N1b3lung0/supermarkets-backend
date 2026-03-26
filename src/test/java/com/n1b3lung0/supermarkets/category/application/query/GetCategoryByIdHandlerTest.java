package com.n1b3lung0.supermarkets.category.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.dto.GetCategoryByIdQuery;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryQueryPort;
import com.n1b3lung0.supermarkets.category.domain.exception.CategoryNotFoundException;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetCategoryByIdHandlerTest {

  private CategoryQueryPort queryPort;
  private GetCategoryByIdHandler handler;

  @BeforeEach
  void setUp() {
    queryPort = mock(CategoryQueryPort.class);
    handler = new GetCategoryByIdHandler(queryPort);
  }

  @Test
  void execute_shouldReturnDetailView_whenCategoryExists() {
    var id = UUID.randomUUID();
    var view =
        new CategoryDetailView(
            id, "Frescos", "10", UUID.randomUUID(), "TOP", null, 0, Instant.now());
    when(queryPort.findDetailById(CategoryId.of(id))).thenReturn(Optional.of(view));

    var result = handler.execute(new GetCategoryByIdQuery(id));

    assertThat(result).isEqualTo(view);
  }

  @Test
  void execute_shouldThrowNotFound_whenCategoryMissing() {
    var id = UUID.randomUUID();
    when(queryPort.findDetailById(CategoryId.of(id))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.execute(new GetCategoryByIdQuery(id)))
        .isInstanceOf(CategoryNotFoundException.class);
  }
}
