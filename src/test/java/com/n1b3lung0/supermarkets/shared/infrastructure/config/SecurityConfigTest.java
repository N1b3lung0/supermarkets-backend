package com.n1b3lung0.supermarkets.shared.infrastructure.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Step 87 — verifies that the {@link SecurityConfig} security rules are enforced correctly.
 *
 * <p>Uses {@code @SpringBootTest} + {@code webAppContextSetup} because {@code @WebMvcTest} was
 * removed in Spring Boot 4. Extends {@link PostgresIntegrationTest} so Flyway migrations run and
 * the real application context loads successfully.
 *
 * <ul>
 *   <li>Protected endpoints (baskets) return 401 without a JWT.
 *   <li>Protected endpoints accept a valid JWT (Spring Security passes the request through).
 *   <li>Public read-only endpoints (supermarkets) return 200 without any auth header.
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest extends PostgresIntegrationTest {

  @Autowired private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  // -------------------------------------------------------------------------
  // Protected endpoints — Baskets (require JWT)
  // -------------------------------------------------------------------------

  @Test
  void createBasket_withoutAuth_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/baskets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"My Basket\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createBasket_withValidJwt_isNotUnauthorized() throws Exception {
    // jwt() bypasses JwtDecoder and injects a pre-built JwtAuthenticationToken.
    // The controller may return 400 (validation) or 201 — either way, NOT 401.
    mockMvc
        .perform(
            post("/api/v1/baskets")
                .with(jwt().jwt(j -> j.subject("user-1")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"My Basket\",\"idempotencyKey\":\"" + UUID.randomUUID() + "\"}"))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void getBasket_withoutAuth_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/baskets/" + UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getBasket_withValidJwt_isNotUnauthorized() throws Exception {
    // Basket does not exist → 404, but NOT 401
    mockMvc
        .perform(get("/api/v1/baskets/" + UUID.randomUUID()).with(jwt()))
        .andExpect(status().isNotFound());
  }

  // -------------------------------------------------------------------------
  // Public endpoints — Supermarkets (GET only, no auth required)
  // -------------------------------------------------------------------------

  @Test
  void listSupermarkets_withoutAuth_returns200() throws Exception {
    // V3 migration seeds 6 supermarkets — this returns 200 with real data
    mockMvc.perform(get("/api/v1/supermarkets")).andExpect(status().isOk());
  }

  @Test
  void getSupermarketById_withoutAuth_isNotUnauthorized() throws Exception {
    // Unknown UUID → 404, but NOT 401
    mockMvc
        .perform(get("/api/v1/supermarkets/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }
}
