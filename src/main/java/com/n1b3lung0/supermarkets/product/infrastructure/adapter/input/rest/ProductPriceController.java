package com.n1b3lung0.supermarkets.product.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductPriceHistoryQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductPriceHistoryUseCase;
import com.n1b3lung0.supermarkets.shared.application.mapper.PageResponseMapper;
import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for product price history queries. */
@RestController
@RequestMapping("/api/v1/products/{productId}/prices")
@Tag(name = "Product Prices", description = "Query product price history")
public class ProductPriceController {

  private final GetProductPriceHistoryUseCase historyUseCase;

  public ProductPriceController(GetProductPriceHistoryUseCase historyUseCase) {
    this.historyUseCase = historyUseCase;
  }

  @GetMapping
  @Operation(
      summary = "Get price history for a product (newest first)",
      description =
          "Returns a paginated, time-ordered list of all price snapshots recorded for the"
              + " given product. Each snapshot includes the price, currency, unit, and the"
              + " timestamp when it was recorded.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Page of price snapshots"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public PageResponse<ProductPriceView> getHistory(
      @Parameter(description = "Product UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID productId,
      @PageableDefault(size = 30, sort = "recordedAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return PageResponseMapper.from(
        historyUseCase.execute(new GetProductPriceHistoryQuery(productId, pageable)));
  }
}
