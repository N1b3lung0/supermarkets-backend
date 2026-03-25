package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import com.n1b3lung0.supermarkets.supermarket.application.dto.GetSupermarketByIdQuery;
import com.n1b3lung0.supermarkets.supermarket.application.dto.ListSupermarketsQuery;
import com.n1b3lung0.supermarkets.supermarket.application.dto.RegisterSupermarketCommand;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketSummaryView;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.command.RegisterSupermarketUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.GetSupermarketByIdUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.ListSupermarketsUseCase;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.input.rest.dto.RegisterSupermarketRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for the Supermarket bounded context. */
@RestController
@RequestMapping("/api/v1/supermarkets")
@Tag(name = "Supermarkets", description = "Manage supermarket chains")
public class SupermarketController {

  private final RegisterSupermarketUseCase registerUseCase;
  private final GetSupermarketByIdUseCase getByIdUseCase;
  private final ListSupermarketsUseCase listUseCase;

  public SupermarketController(
      RegisterSupermarketUseCase registerUseCase,
      GetSupermarketByIdUseCase getByIdUseCase,
      ListSupermarketsUseCase listUseCase) {
    this.registerUseCase = registerUseCase;
    this.getByIdUseCase = getByIdUseCase;
    this.listUseCase = listUseCase;
  }

  @PostMapping
  @Operation(summary = "Register a new supermarket")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Supermarket registered"),
    @ApiResponse(responseCode = "409", description = "Supermarket name already exists"),
    @ApiResponse(responseCode = "422", description = "Validation failed")
  })
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterSupermarketRequest request) {
    var command = new RegisterSupermarketCommand(request.name(), request.country());
    var id = registerUseCase.execute(command);
    return ResponseEntity.created(URI.create("/api/v1/supermarkets/" + id)).build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a supermarket by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Supermarket found"),
    @ApiResponse(responseCode = "404", description = "Supermarket not found")
  })
  public SupermarketDetailView getById(@PathVariable UUID id) {
    return getByIdUseCase.execute(new GetSupermarketByIdQuery(id));
  }

  @GetMapping
  @Operation(summary = "List all supermarkets (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of supermarkets")
  public PageResponse<SupermarketSummaryView> list(
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return listUseCase.execute(new ListSupermarketsQuery(pageable));
  }
}
