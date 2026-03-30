package com.n1b3lung0.supermarkets.supermarket.application.port.output;

import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import java.util.List;
import java.util.Optional;

/** Output port for write operations on the Supermarket aggregate. */
public interface SupermarketRepositoryPort {

  void save(Supermarket supermarket);

  Optional<Supermarket> findById(SupermarketId id);

  boolean existsByName(SupermarketName name);

  /** Returns all non-deleted supermarkets. Used by the scheduler to iterate over all tenants. */
  List<Supermarket> findAllActive();
}
