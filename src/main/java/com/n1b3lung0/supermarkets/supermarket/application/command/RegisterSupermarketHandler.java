package com.n1b3lung0.supermarkets.supermarket.application.command;

import com.n1b3lung0.supermarkets.supermarket.application.dto.RegisterSupermarketCommand;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.command.RegisterSupermarketUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketRepositoryPort;
import com.n1b3lung0.supermarkets.supermarket.domain.exception.DuplicateSupermarketException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketCountry;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import java.util.Objects;

/**
 * Handles the registration of a new Supermarket. No Spring annotations — wired via
 * SupermarketConfig.
 */
public class RegisterSupermarketHandler implements RegisterSupermarketUseCase {

  private final SupermarketRepositoryPort repository;

  public RegisterSupermarketHandler(SupermarketRepositoryPort repository) {
    this.repository = Objects.requireNonNull(repository, "repository is required");
  }

  @Override
  public SupermarketId execute(RegisterSupermarketCommand command) {
    Objects.requireNonNull(command, "command is required");

    var name = SupermarketName.of(command.name());
    var country = SupermarketCountry.of(command.country());

    if (repository.existsByName(name)) {
      throw new DuplicateSupermarketException(name);
    }

    var supermarket = Supermarket.create(name, country);
    repository.save(supermarket);
    // domain events are published by the infrastructure layer after save
    return supermarket.getId();
  }
}
