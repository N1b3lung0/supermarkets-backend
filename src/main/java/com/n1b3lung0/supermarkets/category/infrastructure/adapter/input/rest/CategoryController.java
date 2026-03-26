package com.n1b3lung0.supermarkets.category.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.dto.CategorySummaryView;
import com.n1b3lung0.supermarkets.category.application.dto.GetCategoryByIdQuery;
import com.n1b3lung0.supermarkets.category.application.dto.ListCategoriesQuery;
import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.input.query.GetCategoryByIdUseCase;
import com.n1b3lung0.supermarkets.category.application.port.input.query.ListCategoriesUseCase;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.input.rest.dto.RegisterCategoryRequest;
import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for the Category bounded context. */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Manage product categories")
public class CategoryController {

  private final RegisterCategoryUseCase registerUseCase;
  private final GetCategoryByIdUseCase getByIdUseCase;
  private final ListCategoriesUseCase listUseCase;

  public CategoryController(
      RegisterCategoryUseCase registerUseCase,
      GetCategoryByIdUseCase getByIdUseCase,
      ListCategoriesUseCase listUseCase) {
    this.registerUseCase = registerUseCase;
    this.getByIdUseCase = getByIdUseCase;
    this.listUseCase = listUseCase;
  }

  @PostMapping
  @Operation(summary = "Register a new category")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Category registered"),
    @ApiResponse(
        responseCode = "409",
        description = "Category externalId already exists for supermarket"),
    @ApiResponse(responseCode = "422", description = "Validation failed")
  })
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterCategoryRequest request) {
    var command =
        new RegisterCategoryCommand(
            request.name(),
            request.externalId(),
            request.supermarketId(),
            request.levelType(),
            request.parentId(),
            request.order());
    var id = registerUseCase.execute(command);
    return ResponseEntity.created(URI.create("/api/v1/categories/" + id)).build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a category by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category found"),
    @ApiResponse(responseCode = "404", description = "Category not found")
  })
  public CategoryDetailView getById(@PathVariable UUID id) {
    return getByIdUseCase.execute(new GetCategoryByIdQuery(id));
  }

  @GetMapping
  @Operation(summary = "List categories (paginated), optionally filtered by supermarket / level")
  @ApiResponse(responseCode = "200", description = "Page of categories")
  public PageResponse<CategorySummaryView> list(
      @RequestParam(required = false) UUID supermarketId,
      @RequestParam(required = false) String levelType,
      @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return PageResponse.from(
        listUseCase.execute(new ListCategoriesQuery(supermarketId, levelType, pageable)));
  }
}
