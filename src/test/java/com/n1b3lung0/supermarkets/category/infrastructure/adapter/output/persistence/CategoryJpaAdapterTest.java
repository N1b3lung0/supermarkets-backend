package com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import com.n1b3lung0.supermarkets.category.domain.CategoryMother;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryLevel;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.mapper.CategoryPersistenceMapper;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.repository.SpringCategoryRepository;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryJpaAdapterTest extends PostgresIntegrationTest {

  @Autowired private SpringCategoryRepository springRepository;
  @Autowired private CategoryPersistenceMapper mapper;

  private CategoryJpaAdapter adapter() {
    return new CategoryJpaAdapter(springRepository, mapper);
  }

  // Mercadona is seeded by V3 migration with this fixed UUID
  private static final SupermarketId TEST_SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  @Test
  void save_andFindById_shouldRoundtrip_forTopCategory() {
    // Use a seeded supermarket — grab any from the DB via adapter approach
    // For simplicity, create a stub with a random UUID (no FK enforcement in test TX)
    var adapter = adapter();
    var category = CategoryMother.topCategory(TEST_SUPERMARKET);

    adapter.save(category);
    var found = adapter.findById(category.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo(category.getName());
    assertThat(found.get().getLevel()).isInstanceOf(CategoryLevel.Top.class);
  }

  @Test
  void save_andFindById_shouldRoundtrip_forLeafCategory() {
    var adapter = adapter();
    var top = CategoryMother.topCategory(TEST_SUPERMARKET);
    var sub = CategoryMother.subCategory(TEST_SUPERMARKET, top.getId());
    var leaf = CategoryMother.leafCategory(TEST_SUPERMARKET, sub.getId());

    adapter.save(top);
    adapter.save(sub);
    adapter.save(leaf);

    var found = adapter.findById(leaf.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getLevel()).isInstanceOf(CategoryLevel.Leaf.class);
    assertThat(found.get().getLevel().parentId()).contains(sub.getId());
  }

  @Test
  void existsByExternalIdAndSupermarketId_shouldReturnFalse_whenNotExists() {
    var adapter = adapter();
    assertThat(
            adapter.existsByExternalIdAndSupermarketId(
                ExternalCategoryId.of("NONEXISTENT"), TEST_SUPERMARKET))
        .isFalse();
  }

  @Test
  void existsByExternalIdAndSupermarketId_shouldReturnTrue_afterSave() {
    var adapter = adapter();
    var category = CategoryMother.topCategory(TEST_SUPERMARKET);
    adapter.save(category);

    assertThat(
            adapter.existsByExternalIdAndSupermarketId(
                category.getExternalId(), category.getSupermarketId()))
        .isTrue();
  }

  @Test
  void findDetailById_shouldReturnEmpty_whenNotFound() {
    assertThat(adapter().findDetailById(CategoryId.generate())).isEmpty();
  }

  @Test
  void findAll_shouldReturnSavedCategories() {
    var adapter = adapter();
    adapter.save(CategoryMother.topCategoryWithExternalId(TEST_SUPERMARKET, "TOP-A-1"));
    adapter.save(CategoryMother.topCategoryWithExternalId(TEST_SUPERMARKET, "TOP-B-2"));

    var page = adapter.findAll(null, null, PageRequest.of(0, 10));
    assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
  }
}
