package com.n1b3lung0.supermarkets.category.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryRepositoryPort;
import com.n1b3lung0.supermarkets.category.domain.exception.DuplicateCategoryException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterCategoryHandlerTest {

  private CategoryRepositoryPort repository;
  private RegisterCategoryHandler handler;

  @BeforeEach
  void setUp() {
    repository = mock(CategoryRepositoryPort.class);
    handler = new RegisterCategoryHandler(repository);
  }

  @Test
  void execute_shouldSaveAndReturnId_whenCategoryIsNew() {
    when(repository.existsByExternalIdAndSupermarketId(any(), any())).thenReturn(false);
    var supermarketId = UUID.randomUUID();
    var command = new RegisterCategoryCommand("Frescos", "10", supermarketId, "TOP", null, 0);

    var id = handler.execute(command);

    assertThat(id).isNotNull();
    verify(repository).save(any());
  }

  @Test
  void execute_shouldThrowDuplicate_whenExternalIdExists() {
    when(repository.existsByExternalIdAndSupermarketId(any(), any())).thenReturn(true);
    var supermarketId = UUID.randomUUID();
    var command = new RegisterCategoryCommand("Frescos", "10", supermarketId, "TOP", null, 0);

    assertThatThrownBy(() -> handler.execute(command))
        .isInstanceOf(DuplicateCategoryException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void execute_shouldBuildSubLevel_withParentId() {
    when(repository.existsByExternalIdAndSupermarketId(any(), any())).thenReturn(false);
    var supermarketId = UUID.randomUUID();
    var parentId = UUID.randomUUID();
    var command = new RegisterCategoryCommand("Frutas", "101", supermarketId, "SUB", parentId, 1);

    var id = handler.execute(command);

    assertThat(id).isNotNull();
    verify(repository).save(any());
  }

  @Test
  void execute_shouldThrow_whenLevelTypeIsUnknown() {
    when(repository.existsByExternalIdAndSupermarketId(any(), any())).thenReturn(false);
    var command = new RegisterCategoryCommand("X", "1", UUID.randomUUID(), "INVALID", null, 0);

    assertThatThrownBy(() -> handler.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown levelType");
  }
}
