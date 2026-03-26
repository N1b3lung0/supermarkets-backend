package com.n1b3lung0.supermarkets.category.domain.model;

/**
 * Sealed hierarchy representing the three levels of the Mercadona category tree.
 *
 * <pre>
 *   TOP     — root level    (e.g. "Frescos")
 *   SUB     — second level  (e.g. "Frutas y verduras")
 *   LEAF    — third level   (e.g. "Manzanas")
 * </pre>
 *
 * Modelled as a sealed interface + records so the compiler enforces exhaustive switch.
 */
public sealed interface CategoryLevel
    permits CategoryLevel.Top, CategoryLevel.Sub, CategoryLevel.Leaf {

  /** Root-level category — has no parent. */
  record Top() implements CategoryLevel {}

  /** Mid-level category — parent is a Top. */
  record Sub(CategoryId subParentId) implements CategoryLevel {
    public Sub {
      java.util.Objects.requireNonNull(subParentId, "Sub parentId is required");
    }
  }

  /** Leaf-level category — parent is a Sub. Products are attached here. */
  record Leaf(CategoryId leafParentId) implements CategoryLevel {
    public Leaf {
      java.util.Objects.requireNonNull(leafParentId, "Leaf parentId is required");
    }
  }

  // -------------------------------------------------------------------------
  // Convenience helpers
  // -------------------------------------------------------------------------

  default boolean isTop() {
    return this instanceof Top;
  }

  default boolean isSub() {
    return this instanceof Sub;
  }

  default boolean isLeaf() {
    return this instanceof Leaf;
  }

  default java.util.Optional<CategoryId> parentId() {
    return switch (this) {
      case Top ignored -> java.util.Optional.empty();
      case Sub s -> java.util.Optional.of(s.subParentId());
      case Leaf l -> java.util.Optional.of(l.leafParentId());
    };
  }
}
