package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.comparison.application.port.output.ProductComparisonQueryPort;
import com.n1b3lung0.supermarkets.comparison.domain.model.ProductMatch;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * JDBC adapter for the product comparison query. Uses {@link JdbcClient} (Spring 6.1+) with a
 * native SQL query that joins products with their latest price using {@code DISTINCT ON}.
 *
 * <p>The query uses {@code ILIKE} which benefits from the pg_trgm GIN index created in V9.
 */
public class ProductComparisonJdbcAdapter implements ProductComparisonQueryPort {

  private static final String BASE_QUERY =
      """
      SELECT
          p.id              AS product_id,
          s.id              AS supermarket_id,
          s.name            AS supermarket_name,
          p.name            AS product_name,
          pp.unit_price,
          pp.bulk_price,
          pp.reference_price,
          pp.reference_format,
          pp.currency,
          pp.recorded_at
      FROM products p
      JOIN supermarkets s ON s.id = p.supermarket_id AND s.deleted_at IS NULL
      JOIN LATERAL (
          SELECT unit_price, bulk_price, reference_price, reference_format, currency, recorded_at
          FROM product_prices
          WHERE product_id = p.id
          ORDER BY recorded_at DESC
          LIMIT 1
      ) pp ON true
      WHERE p.is_active = true
        AND p.deleted_at IS NULL
        AND p.name ILIKE :term
      """;

  private static final String SUPERMARKET_FILTER = "AND p.supermarket_id = ANY(:supermarkets)\n";
  private static final String ORDER_BY = "ORDER BY pp.unit_price ASC";

  private final JdbcClient jdbcClient;

  public ProductComparisonJdbcAdapter(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public List<ProductMatch> findMatchesByName(String searchTerm, List<UUID> supermarketIds) {
    var sql = new StringBuilder(BASE_QUERY);
    if (supermarketIds != null && !supermarketIds.isEmpty()) {
      sql.append(SUPERMARKET_FILTER);
    }
    sql.append(ORDER_BY);

    var spec = jdbcClient.sql(sql.toString()).param("term", "%" + searchTerm + "%");
    if (supermarketIds != null && !supermarketIds.isEmpty()) {
      spec = spec.param("supermarkets", supermarketIds.toArray(new UUID[0]));
    }

    return spec.query(
            (rs, rowNum) -> {
              var currency = rs.getString("currency");
              var unitPrice = Money.ofEur(rs.getBigDecimal("unit_price"));
              var bulkRaw = rs.getBigDecimal("bulk_price");
              var bulkPrice = bulkRaw != null ? Money.of(bulkRaw, currency) : null;
              var refRaw = rs.getBigDecimal("reference_price");
              var refPrice = refRaw != null ? Money.of(refRaw, currency) : null;

              return new ProductMatch(
                  (UUID) rs.getObject("product_id"),
                  (UUID) rs.getObject("supermarket_id"),
                  rs.getString("supermarket_name"),
                  rs.getString("product_name"),
                  unitPrice,
                  bulkPrice,
                  refPrice,
                  rs.getString("reference_format"),
                  rs.getObject("recorded_at", Timestamp.class).toInstant());
            })
        .list();
  }

  /** Converts a list of UUIDs to a Postgres UUID array for {@code = ANY(:param)} binding. */
  @SuppressWarnings("unused")
  private static List<UUID> toUuidList(List<UUID> ids) {
    return new ArrayList<>(ids);
  }
}
