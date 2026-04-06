-- V13: convert product_prices to a time-partitioned table (PARTITION BY RANGE recorded_at)
--
-- Strategy:
--   1. Rename existing table to product_prices_legacy
--   2. Create new partitioned parent table with composite PK (id, recorded_at)
--   3. Create DEFAULT partition + monthly partitions (2020-01 through 2027-12)
--   4. Migrate existing data (INSERT INTO ... SELECT *)
--   5. Drop legacy table + old index
--   6. Create index on the new parent table

BEGIN;

-- ---- Step 1: preserve existing data ----------------------------------------
ALTER TABLE product_prices RENAME TO product_prices_legacy;
-- The index idx_product_prices_product_recorded moves with the rename

-- ---- Step 2: create new partitioned parent ----------------------------------
CREATE TABLE product_prices
(
    id                     UUID           NOT NULL,
    product_id             UUID           NOT NULL,
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
    selling_method         SMALLINT       NOT NULL,
    is_new                 BOOLEAN        NOT NULL DEFAULT FALSE,
    is_pack                BOOLEAN        NOT NULL DEFAULT FALSE,
    approx_size            BOOLEAN        NOT NULL DEFAULT FALSE,
    price_decreased        BOOLEAN        NOT NULL DEFAULT FALSE,
    unit_selector          BOOLEAN        NOT NULL DEFAULT FALSE,
    bunch_selector         BOOLEAN        NOT NULL DEFAULT FALSE,
    previous_unit_price    NUMERIC(10, 2),
    min_bunch_amount       NUMERIC(8, 3),
    increment_bunch_amount NUMERIC(8, 3),
    currency               CHAR(3)        NOT NULL DEFAULT 'EUR',
    recorded_at            TIMESTAMPTZ    NOT NULL,
    PRIMARY KEY (id, recorded_at)
) PARTITION BY RANGE (recorded_at);

ALTER TABLE product_prices
    ADD CONSTRAINT fk_product_prices_product
        FOREIGN KEY (product_id) REFERENCES products (id);

-- ---- Step 3a: DEFAULT partition (catches data outside named ranges) ----------
CREATE TABLE product_prices_default PARTITION OF product_prices DEFAULT;

-- ---- Step 3b: monthly partitions 2020-01 → 2027-12 -------------------------
CREATE TABLE product_prices_2020 PARTITION OF product_prices
    FOR VALUES FROM ('2020-01-01') TO ('2021-01-01');
CREATE TABLE product_prices_2021 PARTITION OF product_prices
    FOR VALUES FROM ('2021-01-01') TO ('2022-01-01');
CREATE TABLE product_prices_2022 PARTITION OF product_prices
    FOR VALUES FROM ('2022-01-01') TO ('2023-01-01');
CREATE TABLE product_prices_2023 PARTITION OF product_prices
    FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');
CREATE TABLE product_prices_2024 PARTITION OF product_prices
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
CREATE TABLE product_prices_2025 PARTITION OF product_prices
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
CREATE TABLE product_prices_2026_q1 PARTITION OF product_prices
    FOR VALUES FROM ('2026-01-01') TO ('2026-04-01');
CREATE TABLE product_prices_2026_q2 PARTITION OF product_prices
    FOR VALUES FROM ('2026-04-01') TO ('2026-07-01');
CREATE TABLE product_prices_2026_q3 PARTITION OF product_prices
    FOR VALUES FROM ('2026-07-01') TO ('2026-10-01');
CREATE TABLE product_prices_2026_q4 PARTITION OF product_prices
    FOR VALUES FROM ('2026-10-01') TO ('2027-01-01');
CREATE TABLE product_prices_2027 PARTITION OF product_prices
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');
CREATE TABLE product_prices_2028 PARTITION OF product_prices
    FOR VALUES FROM ('2028-01-01') TO ('2029-01-01');

-- ---- Step 4: migrate existing rows (routes to partition by recorded_at) -----
INSERT INTO product_prices SELECT * FROM product_prices_legacy;

-- ---- Step 5: drop legacy table (also removes its index) ---------------------
DROP TABLE product_prices_legacy;

-- ---- Step 6: index on the partitioned parent (propagates to all partitions) -
CREATE INDEX idx_product_prices_product_recorded
    ON product_prices (product_id, recorded_at DESC);

COMMIT;

