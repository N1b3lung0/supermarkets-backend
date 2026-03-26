-- V4: create categories table
-- Supports a 3-level tree: TOP → SUB → LEAF
-- external_id is the supplier's own integer id (stored as text for portability)
-- (supermarket_id, external_id) is unique so each chain keeps its own id-space

CREATE TABLE categories
(
    id             UUID        NOT NULL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    external_id    VARCHAR(50)  NOT NULL,
    supermarket_id UUID         NOT NULL REFERENCES supermarkets (id),
    level_type     VARCHAR(10)  NOT NULL CHECK (level_type IN ('TOP', 'SUB', 'LEAF')),
    parent_id      UUID         REFERENCES categories (id),
    sort_order     INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);

-- Uniqueness: one externalId per supermarket (soft-delete aware)
CREATE UNIQUE INDEX uq_categories_external_supermarket
    ON categories (supermarket_id, external_id)
    WHERE deleted_at IS NULL;

-- Lookup by supermarket + level (used by list endpoint)
CREATE INDEX idx_categories_supermarket_level
    ON categories (supermarket_id, level_type)
    WHERE deleted_at IS NULL;

-- Lookup by parent (used to build the tree)
CREATE INDEX idx_categories_parent
    ON categories (parent_id)
    WHERE deleted_at IS NULL;

