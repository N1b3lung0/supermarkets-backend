package com.n1b3lung0.supermarkets.sync.application.port.output;

import com.n1b3lung0.supermarkets.sync.application.dto.SyncRunView;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Read-side port — projections for SyncRun. */
public interface SyncRunQueryPort {

  Page<SyncRunView> findBySupermarketId(UUID supermarketId, Pageable pageable);
}
