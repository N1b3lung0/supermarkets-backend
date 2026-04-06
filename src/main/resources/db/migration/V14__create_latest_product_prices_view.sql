-- V14: materialized view for the latest price per product
--
-- Replaces the LATERAL subquery in the comparison adapter with a pre-computed view.
-- The view is refreshed CONCURRENTLY after each sync (requires a UNIQUE index).

CREATE MATERIALIZED VIEW latest_product_prices AS
SELECT DISTINCT ON (product_id)
    id,
    product_id,
    unit_price,
    bulk_price,
    reference_price,
    reference_format,
    currency,
    recorded_at
FROM product_prices
ORDER BY product_id, recorded_at DESC;

-- Required for REFRESH MATERIALIZED VIEW CONCURRENTLY
CREATE UNIQUE INDEX idx_latest_product_prices_product_id
    ON latest_product_prices (product_id);

