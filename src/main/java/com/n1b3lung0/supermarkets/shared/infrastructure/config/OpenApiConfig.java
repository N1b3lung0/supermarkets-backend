package com.n1b3lung0.supermarkets.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration. Available at {@code /swagger-ui.html}.
 *
 * <p>All endpoints require a Bearer JWT token. Click the <strong>Authorize</strong> button and
 * paste a valid JWT to interact with the protected endpoints from Swagger UI.
 *
 * <p>Quick-start for local exploration:
 *
 * <ol>
 *   <li>Six supermarkets are pre-seeded (Mercadona, Carrefour, Alcampo, ALDI, LIDL, DIA).
 *   <li>Trigger a catalog sync via {@code POST /api/v1/sync/supermarkets/{id}} to populate
 *       categories, products, and prices.
 *   <li>Use {@code GET /api/v1/compare?q=leche} to compare prices across supermarkets.
 * </ol>
 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";

  @Bean
  public OpenAPI supermarketsOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Supermarkets Price Comparator API")
                .description(
                    "Backend API for comparing product prices across Spanish"
                        + " supermarkets (Mercadona, Carrefour, Alcampo, ALDI, LIDL, DIA)."
                        + "\n\nAll endpoints require a Bearer JWT token."
                        + " Use the **Authorize** button above to supply it.")
                .version("0.0.1-SNAPSHOT")
                .contact(new Contact().name("n1b3lung0").url("https://github.com/n1b3lung0"))
                .license(new License().name("MIT")))
        // Global JWT bearer security scheme — appears as "Authorize" button in Swagger UI
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_AUTH,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Paste a valid JWT access token (without the 'Bearer' prefix)")))
        // Apply the security scheme to every operation by default
        .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
  }
}
