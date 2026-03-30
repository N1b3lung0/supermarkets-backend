-- V9: pg_trgm extension + GIN index on products.name for fast ILIKE searches
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_products_name_trgm ON products USING GIN (name gin_trgm_ops);

