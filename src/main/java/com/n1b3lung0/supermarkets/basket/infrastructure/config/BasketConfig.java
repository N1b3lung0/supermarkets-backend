package com.n1b3lung0.supermarkets.basket.infrastructure.config;

import com.n1b3lung0.supermarkets.basket.application.command.AddBasketItemHandler;
import com.n1b3lung0.supermarkets.basket.application.command.ClearBasketHandler;
import com.n1b3lung0.supermarkets.basket.application.command.CreateBasketHandler;
import com.n1b3lung0.supermarkets.basket.application.command.RemoveBasketItemHandler;
import com.n1b3lung0.supermarkets.basket.application.command.UpdateBasketItemQuantityHandler;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.AddBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.ClearBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.CreateBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.RemoveBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.UpdateBasketItemQuantityUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.query.GetBasketByIdUseCase;
import com.n1b3lung0.supermarkets.basket.application.query.GetBasketByIdHandler;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.BasketJpaAdapter;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.mapper.BasketPersistenceMapper;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.repository.SpringBasketRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires all Basket beans. Zero Spring annotations in domain or application classes. */
@Configuration
public class BasketConfig {

  @Bean
  public BasketPersistenceMapper basketPersistenceMapper() {
    return new BasketPersistenceMapper();
  }

  @Bean
  public BasketJpaAdapter basketJpaAdapter(
      SpringBasketRepository repository, BasketPersistenceMapper mapper) {
    return new BasketJpaAdapter(repository, mapper);
  }

  @Bean
  public CreateBasketUseCase createBasketUseCase(BasketJpaAdapter adapter) {
    return new CreateBasketHandler(adapter);
  }

  @Bean
  public AddBasketItemUseCase addBasketItemUseCase(BasketJpaAdapter adapter) {
    return new AddBasketItemHandler(adapter);
  }

  @Bean
  public RemoveBasketItemUseCase removeBasketItemUseCase(BasketJpaAdapter adapter) {
    return new RemoveBasketItemHandler(adapter);
  }

  @Bean
  public UpdateBasketItemQuantityUseCase updateBasketItemQuantityUseCase(BasketJpaAdapter adapter) {
    return new UpdateBasketItemQuantityHandler(adapter);
  }

  @Bean
  public ClearBasketUseCase clearBasketUseCase(BasketJpaAdapter adapter) {
    return new ClearBasketHandler(adapter);
  }

  @Bean
  public GetBasketByIdUseCase getBasketByIdUseCase(BasketJpaAdapter adapter) {
    return new GetBasketByIdHandler(adapter);
  }
}
