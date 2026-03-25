package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketCountry;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.mapper.SupermarketPersistenceMapper;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.repository.SpringSupermarketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for SupermarketJpaAdapter using a real PostgreSQL instance (Testcontainers).
 * Spring Boot 4 removed @DataJpaTest; we use @SpringBootTest with a JVM-singleton container.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SupermarketJpaAdapterTest extends PostgresIntegrationTest {

  @Autowired private SpringSupermarketRepository springRepository;

  @Autowired private SupermarketPersistenceMapper mapper;

  private SupermarketJpaAdapter adapter() {
    return new SupermarketJpaAdapter(springRepository, mapper);
  }

  @Test
  void save_andFindById_shouldRoundtripSuccessfully() {
    // given
    var supermarket =
        Supermarket.create(
            SupermarketName.of("TestMarket-" + System.nanoTime()), SupermarketCountry.of("ES"));
    var adapter = adapter();

    // when
    adapter.save(supermarket);
    var found = adapter.findById(supermarket.getId());

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo(supermarket.getName());
    assertThat(found.get().getCountry().value()).isEqualTo("ES");
  }

  @Test
  void existsByName_shouldReturnFalse_whenNameDoesNotExist() {
    // given
    var adapter = adapter();

    // when / then
    assertThat(adapter.existsByName(SupermarketName.of("NonExistent-" + System.nanoTime())))
        .isFalse();
  }

  @Test
  void findDetailById_shouldReturnEmpty_whenNotFound() {
    // given
    var adapter = adapter();

    // when
    var result = adapter.findDetailById(SupermarketId.generate());

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void findAll_shouldReturnNonEmptyPage_afterSeedMigration() {
    // given — V3 seed inserts 6 supermarkets
    var adapter = adapter();

    // when
    var page = adapter.findAll(PageRequest.of(0, 10));

    // then
    assertThat(page.getContent()).isNotEmpty();
  }
}
