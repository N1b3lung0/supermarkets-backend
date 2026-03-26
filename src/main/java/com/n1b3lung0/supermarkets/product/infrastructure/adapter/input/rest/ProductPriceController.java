package com.n1b3lung0.supermarkets.product.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductPriceHistoryQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductPriceHistoryUseCase;
import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
  @Operation(summary = "Get price history for a product (newest first)")
  @ApiResponse(responseCode = "200", description = "Page of price snapshots")
  public PageResponse<ProductPriceView> getHistory(
      @PathVariable UUID productId,
      @PageableDefault(size = 30, sort = "recordedAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return PageResponse.from(
        historyUseCase.execute(new GetProductPriceHistoryQuery(productId, pageable)));
  }
}
