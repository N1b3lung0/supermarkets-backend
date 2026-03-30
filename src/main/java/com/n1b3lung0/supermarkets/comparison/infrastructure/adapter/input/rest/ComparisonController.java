package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductComparisonView;
import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter — exposes the product comparison query endpoint. */
@Tag(name = "Comparison", description = "Cross-supermarket product price comparison")
@RestController
@RequestMapping("/api/v1/compare")
public class ComparisonController {

  private final CompareProductsByNameUseCase compareUseCase;

  public ComparisonController(CompareProductsByNameUseCase compareUseCase) {
    this.compareUseCase = compareUseCase;
  }

  @Operation(summary = "Compare product prices by name across supermarkets")
  @ApiResponse(responseCode = "200", description = "Comparison results sorted by unit price")
  @ApiResponse(responseCode = "400", description = "Missing or blank search term")
  @GetMapping
  public ProductComparisonView compare(
      @RequestParam String q, @RequestParam(required = false) List<UUID> supermarkets) {
    return compareUseCase.execute(new CompareProductsByNameQuery(q, supermarkets));
  }
}
