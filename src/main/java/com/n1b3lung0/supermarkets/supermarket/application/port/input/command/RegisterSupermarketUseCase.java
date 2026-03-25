package com.n1b3lung0.supermarkets.supermarket.application.port.input.command;

import com.n1b3lung0.supermarkets.supermarket.application.dto.RegisterSupermarketCommand;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;

/** Input port for the register-supermarket use case. */
public interface RegisterSupermarketUseCase {

  SupermarketId execute(RegisterSupermarketCommand command);
}
