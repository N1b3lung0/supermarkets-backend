package com.n1b3lung0.supermarkets.category.infrastructure.config;

import com.n1b3lung0.supermarkets.category.application.command.RegisterCategoryHandler;
import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.input.query.GetCategoryByIdUseCase;
import com.n1b3lung0.supermarkets.category.application.port.input.query.ListCategoriesUseCase;
import com.n1b3lung0.supermarkets.category.application.query.GetCategoryByIdHandler;
import com.n1b3lung0.supermarkets.category.application.query.ListCategoriesHandler;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.CategoryJpaAdapter;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.mapper.CategoryPersistenceMapper;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.repository.SpringCategoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires together all Category use cases, handlers, adapters and mappers. Domain and application
 * classes have zero Spring annotations — all wiring happens here.
 */
@Configuration
public class CategoryConfig {

  @Bean
  public CategoryPersistenceMapper categoryPersistenceMapper() {
    return new CategoryPersistenceMapper();
  }

  @Bean
  public CategoryJpaAdapter categoryJpaAdapter(
      SpringCategoryRepository repository, CategoryPersistenceMapper mapper) {
    return new CategoryJpaAdapter(repository, mapper);
  }

  @Bean
  public RegisterCategoryUseCase registerCategoryUseCase(CategoryJpaAdapter adapter) {
    return new RegisterCategoryHandler(adapter);
  }

  @Bean
  public GetCategoryByIdUseCase getCategoryByIdUseCase(CategoryJpaAdapter adapter) {
    return new GetCategoryByIdHandler(adapter);
  }

  @Bean
  public ListCategoriesUseCase listCategoriesUseCase(CategoryJpaAdapter adapter) {
    return new ListCategoriesHandler(adapter);
  }
}
