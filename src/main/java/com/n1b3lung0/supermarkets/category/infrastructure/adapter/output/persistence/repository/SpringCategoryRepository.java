package com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for CategoryEntity. */
public interface SpringCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

  @Query(
      """
            SELECT COUNT(c) FROM CategoryEntity c
            WHERE c.externalId = :externalId
            AND c.supermarketId = :supermarketId
            AND c.deletedAt IS NULL
            """)
  long countByExternalIdAndSupermarketId(
      @Param("externalId") String externalId, @Param("supermarketId") UUID supermarketId);

  @Query(
      """
            SELECT c FROM CategoryEntity c
            WHERE c.externalId = :externalId
            AND c.supermarketId = :supermarketId
            AND c.deletedAt IS NULL
            """)
  Optional<CategoryEntity> findByExternalIdAndSupermarketId(
      @Param("externalId") String externalId, @Param("supermarketId") UUID supermarketId);

  @Query(
      """
            SELECT c FROM CategoryEntity c
            WHERE c.deletedAt IS NULL
            AND c.levelType = :levelType
            AND c.supermarketId = :supermarketId
            """)
  List<CategoryEntity> findByLevelTypeAndSupermarketId(
      @Param("levelType") String levelType, @Param("supermarketId") UUID supermarketId);

  @Query(
      """
            SELECT c FROM CategoryEntity c
            WHERE c.deletedAt IS NULL
            AND (:supermarketId IS NULL OR c.supermarketId = :supermarketId)
            AND (:levelType IS NULL OR c.levelType = :levelType)
            ORDER BY c.sortOrder ASC, c.name ASC
            """)
  Page<CategoryEntity> findAllActive(
      @Param("supermarketId") UUID supermarketId,
      @Param("levelType") String levelType,
      Pageable pageable);

  @Query("SELECT c FROM CategoryEntity c WHERE c.id = :id AND c.deletedAt IS NULL")
  Optional<CategoryEntity> findActiveById(@Param("id") UUID id);
}
