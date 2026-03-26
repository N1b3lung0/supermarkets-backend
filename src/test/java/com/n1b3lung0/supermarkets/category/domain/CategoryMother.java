package com.n1b3lung0.supermarkets.category.domain;

import com.n1b3lung0.supermarkets.category.domain.model.Category;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryLevel;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryName;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryOrder;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;

/** ObjectMother — provides ready-made Category instances for tests. */
public final class CategoryMother {

  private CategoryMother() {}

  public static Category topCategory(SupermarketId supermarketId) {
    return Category.create(
        CategoryName.of("Frescos"),
        ExternalCategoryId.of("10"),
        supermarketId,
        new CategoryLevel.Top(),
        CategoryOrder.zero());
  }

  public static Category topCategoryWithExternalId(SupermarketId supermarketId, String externalId) {
    return Category.create(
        CategoryName.of("Frescos"),
        ExternalCategoryId.of(externalId),
        supermarketId,
        new CategoryLevel.Top(),
        CategoryOrder.zero());
  }

  public static Category subCategory(SupermarketId supermarketId, CategoryId parentId) {
    return Category.create(
        CategoryName.of("Frutas y verduras"),
        ExternalCategoryId.of("101"),
        supermarketId,
        new CategoryLevel.Sub(parentId),
        CategoryOrder.of(1));
  }

  public static Category leafCategory(SupermarketId supermarketId, CategoryId parentId) {
    return Category.create(
        CategoryName.of("Manzanas"),
        ExternalCategoryId.of("1011"),
        supermarketId,
        new CategoryLevel.Leaf(parentId),
        CategoryOrder.of(0));
  }
}
