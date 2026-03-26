package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity.ProductEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for ProductEntity. */
public interface SpringProductRepository extends JpaRepository<ProductEntity, UUID> {

  @Query(
      """
      SELECT p FROM ProductEntity p
      WHERE p.externalId = :externalId
        AND p.supermarketId = :supermarketId
        AND p.deletedAt IS NULL
      """)
  Optional<ProductEntity> findByExternalIdAndSupermarketId(
      @Param("externalId") String externalId, @Param("supermarketId") UUID supermarketId);

  @Query(
      """
      SELECT p.externalId FROM ProductEntity p
      WHERE p.supermarketId = :supermarketId
        AND p.isActive = TRUE
        AND p.deletedAt IS NULL
      """)
  List<String> findActiveExternalIdsBySupermarketId(@Param("supermarketId") UUID supermarketId);

  @Query(
      """
      SELECT p FROM ProductEntity p
      WHERE p.categoryId = :categoryId
        AND p.deletedAt IS NULL
      """)
  Page<ProductEntity> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

  @Query(
      """
      SELECT p FROM ProductEntity p
      WHERE p.supermarketId = :supermarketId
        AND p.deletedAt IS NULL
      """)
  Page<ProductEntity> findBySupermarketId(
      @Param("supermarketId") UUID supermarketId, Pageable pageable);

  @Query("SELECT p FROM ProductEntity p WHERE p.id = :id AND p.deletedAt IS NULL")
  Optional<ProductEntity> findActiveById(@Param("id") UUID id);
}
