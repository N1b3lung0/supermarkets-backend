package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.entity.SupermarketEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for SupermarketEntity. */
public interface SpringSupermarketRepository extends JpaRepository<SupermarketEntity, UUID> {

  @Query("SELECT COUNT(s) FROM SupermarketEntity s WHERE s.name = :name AND s.deletedAt IS NULL")
  long countByName(@Param("name") String name);

  @Query(
      """
            SELECT s FROM SupermarketEntity s
            WHERE s.deletedAt IS NULL
            ORDER BY s.name ASC
            """)
  Page<SupermarketEntity> findAllActive(Pageable pageable);

  @Query("SELECT s FROM SupermarketEntity s WHERE s.id = :id AND s.deletedAt IS NULL")
  java.util.Optional<SupermarketEntity> findActiveById(UUID id);
}
