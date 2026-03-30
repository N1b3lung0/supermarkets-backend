package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence.entity.SyncRunEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for SyncRunEntity. */
public interface SpringSyncRunRepository extends JpaRepository<SyncRunEntity, UUID> {

  @Query(
      """
            SELECT s FROM SyncRunEntity s
            WHERE s.supermarketId = :supermarketId
            ORDER BY s.startedAt DESC
            """)
  Page<SyncRunEntity> findBySupermarketId(
      @Param("supermarketId") UUID supermarketId, Pageable pageable);
}
