package com.n1b3lung0.supermarkets.product.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.n1b3lung0.supermarkets.product.application.dto.RecordProductPriceCommand;
import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.product.application.port.input.command.RecordProductPriceUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.ProductMother;
import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpsertProductHandlerTest {

  @Mock private ProductRepositoryPort productRepository;
  @Mock private RecordProductPriceUseCase recordPrice;

  private UpsertProductCommand buildCommand(String externalId) {
    return new UpsertProductCommand(
        externalId,
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
        0,
        ProductMother.simplePriceInstructions());
  }

  @Test
  void execute_newProduct_shouldSaveAndRecordPrice() {
    given(productRepository.findByExternalIdAndSupermarket(any(), any()))
        .willReturn(Optional.empty());

    var handler = new UpsertProductHandler(productRepository, recordPrice);
    var id = handler.execute(buildCommand("NEW-1"));

    assertThat(id).isNotNull();
    verify(productRepository, times(1)).save(any());
    verify(recordPrice, times(1)).execute(any(RecordProductPriceCommand.class));
  }

  @Test
  void execute_existingProductWithNameChange_shouldSaveAndRecordPrice() {
    var existing =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    existing.pullDomainEvents(); // simulate reconstituted product
    given(
            productRepository.findByExternalIdAndSupermarket(
                ExternalProductId.of("3400"), ProductMother.DEFAULT_SUPERMARKET))
        .willReturn(Optional.of(existing));

    var command =
        new UpsertProductCommand(
            "3400",
            ProductMother.DEFAULT_SUPERMARKET.value(),
            ProductMother.DEFAULT_CATEGORY.value(),
            "Leche Semi Desnatada", // changed name
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
            0,
            ProductMother.simplePriceInstructions());

    var handler = new UpsertProductHandler(productRepository, recordPrice);
    handler.execute(command);

    verify(productRepository, times(1)).save(any());
    verify(recordPrice, times(1)).execute(any());
  }

  @Test
  void execute_existingProductNoChanges_shouldNotSaveButAlwaysRecordPrice() {
    var existing =
        ProductMother.simpleProduct(
            ProductMother.DEFAULT_SUPERMARKET, ProductMother.DEFAULT_CATEGORY);
    existing.pullDomainEvents(); // simulate reconstituted product (no pending events)
    given(productRepository.findByExternalIdAndSupermarket(any(), any()))
        .willReturn(Optional.of(existing));

    // same data as simpleProduct
    var command =
        new UpsertProductCommand(
            "3400",
            ProductMother.DEFAULT_SUPERMARKET.value(),
            ProductMother.DEFAULT_CATEGORY.value(),
            "Leche Entera",
            "Leche Entera UHT",
            "Leche entera de calidad",
            "Hacendado",
            "8410011015827",
            "España",
            "Brik",
            "https://prod.static9.net.au/fs/1234.jpg",
            "Conservar en lugar fresco",
            null,
            null,
            null,
            null,
            "Contiene lactosa",
            "Leche entera",
            List.of(),
            false,
            false,
            false,
            false,
            0,
            ProductMother.simplePriceInstructions());

    var handler = new UpsertProductHandler(productRepository, recordPrice);
    handler.execute(command);

    verify(productRepository, never()).save(any());
    verify(recordPrice, times(1)).execute(any());
  }

  @Test
  void execute_nullCommand_shouldThrow() {
    var handler = new UpsertProductHandler(productRepository, recordPrice);
    assertThatThrownBy(() -> handler.execute(null)).isInstanceOf(NullPointerException.class);
  }
}
