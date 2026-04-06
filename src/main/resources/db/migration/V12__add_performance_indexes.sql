-- V12: additional performance indexes for common query patterns

-- Composite index for products filtered by supermarket + category (used in sync & list queries)
CREATE INDEX idx_products_supermarket_category
    ON products (supermarket_id, category_id)
    WHERE deleted_at IS NULL;

-- Partial index for active products per supermarket (used in deactivation + list queries)
CREATE INDEX idx_products_supermarket_active
    ON products (supermarket_id)
    WHERE is_active = TRUE AND deleted_at IS NULL;

-- Partial index for external ID lookup per supermarket (used in sync upsert)
CREATE INDEX idx_products_external_supermarket
    ON products (external_id, supermarket_id)
    WHERE deleted_at IS NULL;

