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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Basket created; Location header contains the URI"),
    @ApiResponse(responseCode = "400", description = "Validation failed — name is blank"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
  })
  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody CreateBasketRequest request) {
    var id = createBasket.execute(new CreateBasketCommand(request.name()));
    return ResponseEntity.created(URI.create("/api/v1/baskets/" + id.value())).build();
  }

  @Operation(summary = "Get basket by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Basket found"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Basket not found")
  })
  @GetMapping("/{id}")
  public BasketDetailView getById(
      @Parameter(description = "Basket UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID id) {
    return getById.execute(new GetBasketByIdQuery(id));
  }

  @Operation(summary = "Add an item to a basket")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Item added; Location header contains the item URI"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed — blank name or quantity < 1"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Basket not found"),
    @ApiResponse(
        responseCode = "409",
        description = "An item with the same product name already exists in this basket")
  })
  @PostMapping("/{id}/items")
  public ResponseEntity<Void> addItem(
      @Parameter(description = "Basket UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID id,
      @Valid @RequestBody AddItemRequest request) {
    var itemId =
        addItem.execute(new AddBasketItemCommand(id, request.productName(), request.quantity()));
    return ResponseEntity.created(URI.create("/api/v1/baskets/" + id + "/items/" + itemId.value()))
        .build();
  }

  @Operation(summary = "Update item quantity")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Quantity updated"),
    @ApiResponse(responseCode = "400", description = "Validation failed — quantity < 1"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Basket or item not found")
  })
  @PatchMapping("/{id}/items/{itemId}")
  public ResponseEntity<Void> updateQuantity(
      @Parameter(description = "Basket UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID id,
      @Parameter(description = "Item UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afb7")
          @PathVariable
          UUID itemId,
      @Valid @RequestBody UpdateQuantityRequest request) {
    updateQuantity.execute(new UpdateBasketItemQuantityCommand(id, itemId, request.quantity()));
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Remove item from basket")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Item removed"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Basket or item not found")
  })
  @DeleteMapping("/{id}/items/{itemId}")
  public ResponseEntity<Void> removeItem(
      @Parameter(description = "Basket UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID id,
      @Parameter(description = "Item UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afb7")
          @PathVariable
          UUID itemId) {
    removeItem.execute(new RemoveBasketItemCommand(id, itemId));
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Clear all items from basket")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "All items removed"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Basket not found")
  })
  @DeleteMapping("/{id}/items")
  public ResponseEntity<Void> clearBasket(
      @Parameter(description = "Basket UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID id) {
    clearBasket.execute(new ClearBasketCommand(id));
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Compare basket total cost across all supermarkets",
      description =
          "Returns the total cost of the basket for each supermarket that has prices"
              + " for all items, ranked from cheapest to most expensive.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Comparison result with per-supermarket totals"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Basket not found")
  })
  @GetMapping("/{id}/compare")
  public BasketComparisonView compare(
      @Parameter(description = "Basket UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID id) {
    return compareBasket.execute(new CompareBasketQuery(id));
  }

  // --- inner request records ---

  @Schema(description = "Payload to create a new basket")
  public record CreateBasketRequest(
      @Schema(description = "Descriptive name for the basket", example = "Compra semanal") @NotBlank
          String name) {}

  @Schema(description = "Payload to add a product item to a basket")
  public record AddItemRequest(
      @Schema(
              description =
                  "Product name used to search across supermarkets (fuzzy match via ILIKE)",
              example = "Leche entera")
          @NotBlank
          String productName,
      @Schema(description = "Number of units", example = "2") @Min(1) int quantity) {}

  @Schema(description = "Payload to update the quantity of a basket item")
  public record UpdateQuantityRequest(
      @Schema(description = "New quantity (must be at least 1)", example = "3") @Min(1)
          int quantity) {}
}
