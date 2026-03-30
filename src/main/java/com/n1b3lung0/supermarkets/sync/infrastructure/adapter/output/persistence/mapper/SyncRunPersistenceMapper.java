package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncRunView;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRun;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncStatus;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.entity.SyncRunEntity;

/** Maps between SyncRun domain aggregate and SyncRunEntity / SyncRunView. */
public class SyncRunPersistenceMapper {

  public SyncRunEntity toEntity(SyncRun domain) {
    var entity = new SyncRunEntity();
    entity.setId(domain.getId().value());
    entity.setSupermarketId(domain.getSupermarketId().value());
    entity.setStartedAt(domain.getStartedAt());
    entity.setFinishedAt(domain.getFinishedAt());
    entity.setStatus(domain.getStatus().name());
    entity.setCategoriesSynced(domain.getCategoriesSynced());
    entity.setProductsSynced(domain.getProductsSynced());
    entity.setProductsDeactivated(domain.getProductsDeactivated());
    entity.setErrorMessage(domain.getErrorMessage());
    return entity;
  }

  public SyncRun toDomain(SyncRunEntity entity) {
    return SyncRun.reconstitute(
        SyncRunId.of(entity.getId()),
        SupermarketId.of(entity.getSupermarketId()),
        entity.getStartedAt(),
        entity.getFinishedAt(),
        SyncStatus.valueOf(entity.getStatus()),
        entity.getCategoriesSynced(),
        entity.getProductsSynced(),
        entity.getProductsDeactivated(),
        entity.getErrorMessage());
  }

  public SyncRunView toView(SyncRunEntity entity) {
    return new SyncRunView(
        entity.getId(),
        entity.getSupermarketId(),
        entity.getStartedAt(),
        entity.getFinishedAt(),
        entity.getStatus(),
        entity.getCategoriesSynced(),
        entity.getProductsSynced(),
        entity.getProductsDeactivated(),
        entity.getErrorMessage());
  }
}
