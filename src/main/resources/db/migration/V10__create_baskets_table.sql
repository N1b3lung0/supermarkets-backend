-- V10: basket tables
CREATE TABLE baskets
(
    id         UUID         NOT NULL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE basket_items
(
    id           UUID         NOT NULL PRIMARY KEY,
    basket_id    UUID         NOT NULL REFERENCES baskets (id) ON DELETE CASCADE,
    product_name VARCHAR(255) NOT NULL,
    quantity     INTEGER      NOT NULL CHECK (quantity > 0),
    CONSTRAINT uq_basket_item_product UNIQUE (basket_id, product_name)
);

CREATE INDEX idx_basket_items_basket ON basket_items (basket_id);

