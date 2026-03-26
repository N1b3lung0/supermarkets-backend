-- V6: create product_prices table (append-only price history)
-- Each scraper run appends a new row — rows are never updated.

CREATE TABLE product_prices
(
    id                     UUID          NOT NULL PRIMARY KEY,
    product_id             UUID          NOT NULL REFERENCES products (id),
    unit_price             NUMERIC(10, 2) NOT NULL,
    bulk_price             NUMERIC(10, 2),
    reference_price        NUMERIC(10, 2),
    reference_format       VARCHAR(20),
    size_format            VARCHAR(20),
    unit_size              NUMERIC(8, 3),
    unit_name              VARCHAR(20),
    total_units            INTEGER,
    pack_size              INTEGER,
    iva                    INTEGER,
    tax_percentage         VARCHAR(10),
    selling_method         SMALLINT      NOT NULL,
    is_new                 BOOLEAN       NOT NULL DEFAULT FALSE,
    is_pack                BOOLEAN       NOT NULL DEFAULT FALSE,
    approx_size            BOOLEAN       NOT NULL DEFAULT FALSE,
    price_decreased        BOOLEAN       NOT NULL DEFAULT FALSE,
    unit_selector          BOOLEAN       NOT NULL DEFAULT FALSE,
    bunch_selector         BOOLEAN       NOT NULL DEFAULT FALSE,
    previous_unit_price    NUMERIC(10, 2),
    min_bunch_amount       NUMERIC(8, 3),
    increment_bunch_amount NUMERIC(8, 3),
    currency               CHAR(3)       NOT NULL DEFAULT 'EUR',
    recorded_at            TIMESTAMPTZ   NOT NULL
);

-- Primary query pattern: latest price for a product + paginated history
CREATE INDEX idx_product_prices_product_recorded ON product_prices (product_id, recorded_at DESC);

