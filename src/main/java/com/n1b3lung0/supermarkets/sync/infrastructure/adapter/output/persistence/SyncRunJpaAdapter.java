package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.sync.application.dto.SyncRunView;
import com.n1b3lung0.supermarkets.sync.application.port.output.SyncRunQueryPort;
import com.n1b3lung0.supermarkets.sync.application.port.output.SyncRunRepositoryPort;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRun;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.mapper.SyncRunPersistenceMapper;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.repository.SpringSyncRunRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter implementing both write and read ports for SyncRun. */
public class SyncRunJpaAdapter implements SyncRunRepositoryPort, SyncRunQueryPort {

  private final SpringSyncRunRepository repository;
  private final SyncRunPersistenceMapper mapper;

  public SyncRunJpaAdapter(SpringSyncRunRepository repository, SyncRunPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public void save(SyncRun syncRun) {
    repository.save(mapper.toEntity(syncRun));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SyncRun> findById(SyncRunId id) {
    return repository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SyncRunView> findBySupermarketId(UUID supermarketId, Pageable pageable) {
    return repository.findBySupermarketId(supermarketId, pageable).map(mapper::toView);
  }
}
