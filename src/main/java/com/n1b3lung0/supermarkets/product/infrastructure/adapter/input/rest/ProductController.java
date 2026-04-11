package com.n1b3lung0.supermarkets.product.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductByIdQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ListProductsByCategoryQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ListProductsBySupermarketQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductByIdUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsByCategoryUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsBySupermarketUseCase;
import com.n1b3lung0.supermarkets.shared.application.mapper.PageResponseMapper;
import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for product queries. */
@RestController
@Tag(name = "Products", description = "Query product catalogue")
public class ProductController {

  private final GetProductByIdUseCase getByIdUseCase;
  private final ListProductsByCategoryUseCase listByCategoryUseCase;
  private final ListProductsBySupermarketUseCase listBySupermarketUseCase;

  public ProductController(
      GetProductByIdUseCase getByIdUseCase,
      ListProductsByCategoryUseCase listByCategoryUseCase,
      ListProductsBySupermarketUseCase listBySupermarketUseCase) {
    this.getByIdUseCase = getByIdUseCase;
    this.listByCategoryUseCase = listByCategoryUseCase;
    this.listBySupermarketUseCase = listBySupermarketUseCase;
  }

  @GetMapping("/api/v1/products/{id}")
  @Operation(summary = "Get a product by ID (includes latest price)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Product found"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ProductDetailView getById(@PathVariable UUID id) {
    return getByIdUseCase.execute(new GetProductByIdQuery(id));
  }

  @GetMapping("/api/v1/supermarkets/{supermarketId}/products")
  @Operation(summary = "List products by supermarket (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of products")
  public PageResponse<ProductSummaryView> listBySupermarket(
      @PathVariable UUID supermarketId,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return PageResponseMapper.from(
        listBySupermarketUseCase.execute(
            new ListProductsBySupermarketQuery(supermarketId, pageable)));
  }

  @GetMapping("/api/v1/categories/{categoryId}/products")
  @Operation(summary = "List products by category (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of products")
  public PageResponse<ProductSummaryView> listByCategory(
      @PathVariable UUID categoryId,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return PageResponseMapper.from(
        listByCategoryUseCase.execute(new ListProductsByCategoryQuery(categoryId, pageable)));
  }
}
