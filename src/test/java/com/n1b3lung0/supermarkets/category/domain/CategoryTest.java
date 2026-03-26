package com.n1b3lung0.supermarkets.category.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.n1b3lung0.supermarkets.category.domain.event.CategoryRegistered;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryLevel;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryName;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryOrder;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryTest {

  private static final SupermarketId SUPERMARKET_ID = SupermarketId.of(UUID.randomUUID());

  // -------------------------------------------------------------------------
  // CategoryName VO
  // -------------------------------------------------------------------------

  @Test
  void categoryName_shouldRejectBlank() {
    assertThatThrownBy(() -> CategoryName.of("  ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void categoryName_shouldRejectOver255Chars() {
    assertThatThrownBy(() -> CategoryName.of("a".repeat(256)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // -------------------------------------------------------------------------
  // ExternalCategoryId VO
  // -------------------------------------------------------------------------

  @Test
  void externalCategoryId_shouldRejectBlank() {
    assertThatThrownBy(() -> ExternalCategoryId.of(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void externalCategoryId_of_int_shouldWork() {
    assertThat(ExternalCategoryId.of(42).value()).isEqualTo("42");
  }

  // -------------------------------------------------------------------------
  // CategoryOrder VO
  // -------------------------------------------------------------------------

  @Test
  void categoryOrder_shouldRejectNegative() {
    assertThatThrownBy(() -> CategoryOrder.of(-1)).isInstanceOf(IllegalArgumentException.class);
  }

  // -------------------------------------------------------------------------
  // CategoryLevel sealed hierarchy
  // -------------------------------------------------------------------------

  @Test
  void categoryLevel_top_shouldHaveNoParent() {
    var level = new CategoryLevel.Top();
    assertThat(level.isTop()).isTrue();
    assertThat(level.parentId()).isEmpty();
  }

  @Test
  void categoryLevel_sub_shouldRequireParentId() {
    var parentId = CategoryId.generate();
    var level = new CategoryLevel.Sub(parentId);
    assertThat(level.isSub()).isTrue();
    assertThat(level.parentId()).contains(parentId);
  }

  @Test
  void categoryLevel_leaf_shouldRequireParentId() {
    var parentId = CategoryId.generate();
    var level = new CategoryLevel.Leaf(parentId);
    assertThat(level.isLeaf()).isTrue();
    assertThat(level.parentId()).contains(parentId);
  }

  // -------------------------------------------------------------------------
  // Category aggregate
  // -------------------------------------------------------------------------

  @Test
  void create_shouldEmitCategoryRegisteredEvent() {
    var category = CategoryMother.topCategory(SUPERMARKET_ID);
    var events = category.pullDomainEvents();

    assertThat(events).hasSize(1);
    assertThat(events.getFirst()).isInstanceOf(CategoryRegistered.class);
    var event = (CategoryRegistered) events.getFirst();
    assertThat(event.categoryId()).isEqualTo(category.getId());
    assertThat(event.supermarketId()).isEqualTo(SUPERMARKET_ID);
  }

  @Test
  void pullDomainEvents_shouldClearEvents() {
    var category = CategoryMother.topCategory(SUPERMARKET_ID);
    category.pullDomainEvents();
    assertThat(category.pullDomainEvents()).isEmpty();
  }

  @Test
  void rename_shouldUpdateName() {
    var category = CategoryMother.topCategory(SUPERMARKET_ID);
    category.rename(CategoryName.of("New Name"));
    assertThat(category.getName().value()).isEqualTo("New Name");
  }

  @Test
  void reorder_shouldUpdateOrder() {
    var category = CategoryMother.topCategory(SUPERMARKET_ID);
    category.reorder(CategoryOrder.of(5));
    assertThat(category.getOrder().value()).isEqualTo(5);
  }

  @Test
  void create_shouldAssignGeneratedId() {
    var a = CategoryMother.topCategory(SUPERMARKET_ID);
    var b = CategoryMother.topCategory(SUPERMARKET_ID);
    assertThat(a.getId()).isNotEqualTo(b.getId());
  }
}
