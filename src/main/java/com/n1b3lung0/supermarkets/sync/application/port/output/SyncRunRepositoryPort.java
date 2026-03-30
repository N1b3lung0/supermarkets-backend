package com.n1b3lung0.supermarkets.sync.application.port.output;

import com.n1b3lung0.supermarkets.sync.domain.model.SyncRun;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;
import java.util.Optional;

/** Write-side port — persistence operations for SyncRun. */
public interface SyncRunRepositoryPort {

  void save(SyncRun syncRun);

  Optional<SyncRun> findById(SyncRunId id);
}
