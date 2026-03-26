-- V5: create products and product_suppliers tables
-- products: one row per product per supermarket (external_id + supermarket_id is unique)
-- product_suppliers: ordered list of supplier names for each product

CREATE TABLE products
(
    id                   UUID          NOT NULL PRIMARY KEY,
    external_id          VARCHAR(50)   NOT NULL,
    supermarket_id       UUID          NOT NULL REFERENCES supermarkets (id),
    category_id          UUID          NOT NULL REFERENCES categories (id),
    name                 VARCHAR(255)  NOT NULL,
    legal_name           VARCHAR(255),
    description          VARCHAR(2000),
    brand                VARCHAR(255),
    ean                  VARCHAR(30),
    origin               VARCHAR(500),
    packaging            VARCHAR(100),
    thumbnail_url        VARCHAR(1000),
    storage_instructions VARCHAR(500),
    usage_instructions   VARCHAR(500),
    mandatory_mentions   VARCHAR(1000),
    production_variant   VARCHAR(500),
    danger_mentions      VARCHAR(1000),
    allergens            VARCHAR(2000),
    ingredients          VARCHAR(2000),
    is_water             BOOLEAN       NOT NULL DEFAULT FALSE,
    requires_age_check   BOOLEAN       NOT NULL DEFAULT FALSE,
    is_bulk              BOOLEAN       NOT NULL DEFAULT FALSE,
    is_variable_weight   BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active            BOOLEAN       NOT NULL DEFAULT TRUE,
    purchase_limit       INTEGER       NOT NULL DEFAULT 999,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted_at           TIMESTAMPTZ,
    CONSTRAINT uq_product_external_supermarket UNIQUE (external_id, supermarket_id)
);

CREATE TABLE product_suppliers
(
    product_id UUID         NOT NULL REFERENCES products (id),
    name       VARCHAR(255) NOT NULL,
    position   SMALLINT     NOT NULL,
    PRIMARY KEY (product_id, position)
);

-- Lookup by supermarket (sync + list endpoint)
CREATE INDEX idx_products_supermarket ON products (supermarket_id) WHERE deleted_at IS NULL;

-- Lookup by category (list endpoint)
CREATE INDEX idx_products_category ON products (category_id) WHERE deleted_at IS NULL;

