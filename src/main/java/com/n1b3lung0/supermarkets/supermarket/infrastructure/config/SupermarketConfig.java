package com.n1b3lung0.supermarkets.supermarket.infrastructure.config;

import com.n1b3lung0.supermarkets.supermarket.application.command.RegisterSupermarketHandler;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.command.RegisterSupermarketUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.GetSupermarketByIdUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.ListSupermarketsUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.query.GetSupermarketByIdHandler;
import com.n1b3lung0.supermarkets.supermarket.application.query.ListSupermarketsHandler;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.SupermarketJpaAdapter;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.mapper.SupermarketPersistenceMapper;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.repository.SpringSupermarketRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires together all Supermarket use cases, handlers, adapters, and mappers. Domain and application
 * classes have zero Spring annotations — all wiring happens here.
 */
@Configuration
public class SupermarketConfig {

  @Bean
  public SupermarketPersistenceMapper supermarketPersistenceMapper() {
    return new SupermarketPersistenceMapper();
  }

  @Bean
  public SupermarketJpaAdapter supermarketJpaAdapter(
      SpringSupermarketRepository repository, SupermarketPersistenceMapper mapper) {
    return new SupermarketJpaAdapter(repository, mapper);
  }

  @Bean
  public RegisterSupermarketUseCase registerSupermarketUseCase(SupermarketJpaAdapter adapter) {
    return new RegisterSupermarketHandler(adapter);
  }

  @Bean
  public GetSupermarketByIdUseCase getSupermarketByIdUseCase(SupermarketJpaAdapter adapter) {
    return new GetSupermarketByIdHandler(adapter);
  }

  @Bean
  public ListSupermarketsUseCase listSupermarketsUseCase(SupermarketJpaAdapter adapter) {
    return new ListSupermarketsHandler(adapter);
  }
}
