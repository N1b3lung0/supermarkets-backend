package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.shared.application.mapper.PageResponseMapper;
import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncRunView;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncSupermarketCatalogCommand;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import com.n1b3lung0.supermarkets.sync.application.port.output.SyncRunQueryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter — triggers catalog syncs and queries sync run history. */
@Tag(name = "Sync", description = "Catalog sync operations")
@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

  private final SyncSupermarketCatalogUseCase syncUseCase;
  private final SyncRunQueryPort syncRunQueryPort;

  public SyncController(
      SyncSupermarketCatalogUseCase syncUseCase, SyncRunQueryPort syncRunQueryPort) {
    this.syncUseCase = syncUseCase;
    this.syncRunQueryPort = syncRunQueryPort;
  }

  @Operation(
      summary = "Trigger a full catalog sync for a supermarket",
      description =
          "Fetches the latest product catalogue from the supermarket's external API and"
              + " persists categories, products, and prices. The operation runs synchronously"
              + " — the response is returned only once the sync is complete.")
  @ApiResponses({
    @ApiResponse(responseCode = "202", description = "Sync accepted and started"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
    @ApiResponse(responseCode = "404", description = "Supermarket not found")
  })
  @PostMapping("/supermarkets/{supermarketId}")
  public ResponseEntity<Void> triggerSync(
      @Parameter(
              description = "UUID of the supermarket to sync",
              example = "00000000-0000-0000-0000-000000000001")
          @PathVariable
          UUID supermarketId) {
    syncUseCase.execute(new SyncSupermarketCatalogCommand(supermarketId));
    return ResponseEntity.accepted().build();
  }

  @Operation(summary = "List sync runs for a supermarket")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Paginated list of sync runs"),
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
  })
  @GetMapping("/runs")
  public PageResponse<SyncRunView> listRuns(
      @Parameter(
              description = "UUID of the supermarket whose sync history is requested",
              example = "00000000-0000-0000-0000-000000000001",
              required = true)
          @RequestParam
          UUID supermarketId,
      @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return PageResponseMapper.from(syncRunQueryPort.findBySupermarketId(supermarketId, pageable));
  }
}
