package com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.entity.BasketEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringBasketRepository extends JpaRepository<BasketEntity, UUID> {

  @Query("SELECT b FROM BasketEntity b LEFT JOIN FETCH b.items WHERE b.id = :id")
  Optional<BasketEntity> findByIdWithItems(UUID id);
}
