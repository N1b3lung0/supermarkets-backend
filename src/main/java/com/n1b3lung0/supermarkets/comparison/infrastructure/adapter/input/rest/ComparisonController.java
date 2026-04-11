package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductComparisonView;
import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

  @Operation(
      summary = "Compare product prices by name across supermarkets",
      description =
          "Searches for products whose name contains the given query (case-insensitive) and"
              + " returns a ranked list sorted by unit price ascending."
              + " Optionally restrict the search to a subset of supermarkets by supplying"
              + " their UUIDs via the `supermarkets` parameter.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Comparison results sorted by unit price"),
    @ApiResponse(responseCode = "400", description = "Missing or blank search term"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
  })
  @GetMapping
  public ProductComparisonView compare(
      @Parameter(
              description = "Product name search term (minimum 2 characters)",
              example = "leche entera",
              required = true)
          @RequestParam
          String q,
      @Parameter(
              description =
                  "Optional list of supermarket UUIDs to restrict the search."
                      + " When omitted, all supermarkets are searched.",
              example = "[\"00000000-0000-0000-0000-000000000001\"]")
          @RequestParam(required = false)
          List<UUID> supermarkets) {
    return compareUseCase.execute(new CompareProductsByNameQuery(q, supermarkets));
  }
}
