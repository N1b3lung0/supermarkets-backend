package com.n1b3lung0.supermarkets.basket.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.basket.application.dto.AddBasketItemCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.BasketComparisonView;
import com.n1b3lung0.supermarkets.basket.application.dto.BasketDetailView;
import com.n1b3lung0.supermarkets.basket.application.dto.ClearBasketCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.CompareBasketQuery;
import com.n1b3lung0.supermarkets.basket.application.dto.CreateBasketCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.GetBasketByIdQuery;
import com.n1b3lung0.supermarkets.basket.application.dto.RemoveBasketItemCommand;
import com.n1b3lung0.supermarkets.basket.application.dto.UpdateBasketItemQuantityCommand;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.AddBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.ClearBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.CreateBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.RemoveBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.UpdateBasketItemQuantityUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.query.CompareBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.query.GetBasketByIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter — CRUD operations for shopping baskets. */
@Tag(name = "Basket", description = "Shopping basket management")
@RestController
@RequestMapping("/api/v1/baskets")
public class BasketController {

  private final CreateBasketUseCase createBasket;
  private final AddBasketItemUseCase addItem;
  private final RemoveBasketItemUseCase removeItem;
  private final UpdateBasketItemQuantityUseCase updateQuantity;
  private final ClearBasketUseCase clearBasket;
  private final GetBasketByIdUseCase getById;
  private final CompareBasketUseCase compareBasket;

  public BasketController(
      CreateBasketUseCase createBasket,
      AddBasketItemUseCase addItem,
      RemoveBasketItemUseCase removeItem,
      UpdateBasketItemQuantityUseCase updateQuantity,
      ClearBasketUseCase clearBasket,
      GetBasketByIdUseCase getById,
      CompareBasketUseCase compareBasket) {
    this.createBasket = createBasket;
    this.addItem = addItem;
    this.removeItem = removeItem;
    this.updateQuantity = updateQuantity;
    this.clearBasket = clearBasket;
    this.getById = getById;
    this.compareBasket = compareBasket;
  }

  @Operation(summary = "Create a new basket")
  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody CreateBasketRequest request) {
    var id = createBasket.execute(new CreateBasketCommand(request.name()));
    return ResponseEntity.created(URI.create("/api/v1/baskets/" + id.value())).build();
  }

  @Operation(summary = "Get basket by id")
  @GetMapping("/{id}")
  public BasketDetailView getById(@PathVariable UUID id) {
    return getById.execute(new GetBasketByIdQuery(id));
  }

  @Operation(summary = "Add an item to a basket")
  @PostMapping("/{id}/items")
  public ResponseEntity<Void> addItem(
      @PathVariable UUID id, @Valid @RequestBody AddItemRequest request) {
    var itemId =
        addItem.execute(new AddBasketItemCommand(id, request.productName(), request.quantity()));
    return ResponseEntity.created(URI.create("/api/v1/baskets/" + id + "/items/" + itemId.value()))
        .build();
  }

  @Operation(summary = "Update item quantity")
  @PatchMapping("/{id}/items/{itemId}")
  public ResponseEntity<Void> updateQuantity(
      @PathVariable UUID id,
      @PathVariable UUID itemId,
      @Valid @RequestBody UpdateQuantityRequest request) {
    updateQuantity.execute(new UpdateBasketItemQuantityCommand(id, itemId, request.quantity()));
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Remove item from basket")
  @DeleteMapping("/{id}/items/{itemId}")
  public ResponseEntity<Void> removeItem(@PathVariable UUID id, @PathVariable UUID itemId) {
    removeItem.execute(new RemoveBasketItemCommand(id, itemId));
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Clear all items from basket")
  @DeleteMapping("/{id}/items")
  public ResponseEntity<Void> clearBasket(@PathVariable UUID id) {
    clearBasket.execute(new ClearBasketCommand(id));
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Compare basket total cost across all supermarkets")
  @GetMapping("/{id}/compare")
  public BasketComparisonView compare(@PathVariable UUID id) {
    return compareBasket.execute(new CompareBasketQuery(id));
  }

  // --- inner request records ---

  public record CreateBasketRequest(@NotBlank String name) {}

  public record AddItemRequest(@NotBlank String productName, @Min(1) int quantity) {}

  public record UpdateQuantityRequest(@Min(1) int quantity) {}
}
