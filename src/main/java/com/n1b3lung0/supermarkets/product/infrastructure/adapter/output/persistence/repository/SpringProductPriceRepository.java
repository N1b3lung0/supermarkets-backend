package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.entity.ProductPriceEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for ProductPriceEntity. */
public interface SpringProductPriceRepository extends JpaRepository<ProductPriceEntity, UUID> {

  @Query(
      """
      SELECT p FROM ProductPriceEntity p
      WHERE p.productId = :productId
      ORDER BY p.recordedAt DESC
      LIMIT 1
      """)
  Optional<ProductPriceEntity> findLatestByProductId(@Param("productId") UUID productId);

  @Query(
      """
      SELECT p FROM ProductPriceEntity p
      WHERE p.productId = :productId
      ORDER BY p.recordedAt DESC
      """)
  Page<ProductPriceEntity> findHistoryByProductId(
      @Param("productId") UUID productId, Pageable pageable);
}
