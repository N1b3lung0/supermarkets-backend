package com.n1b3lung0.supermarkets.sync.application.port.input.command;

import com.n1b3lung0.supermarkets.sync.application.dto.SyncSupermarketCatalogCommand;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;

/** Use case — orchestrates a full catalog sync (categories + products) for one supermarket. */
public interface SyncSupermarketCatalogUseCase {

  /**
   * Executes the sync and returns the id of the created {@link
   * com.n1b3lung0.supermarkets.sync.domain.model.SyncRun}.
   */
  SyncRunId execute(SyncSupermarketCatalogCommand command);
}
