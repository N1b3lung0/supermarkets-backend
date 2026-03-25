package com.n1b3lung0.supermarkets.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI / Swagger UI configuration. Available at /swagger-ui.html */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI supermarketsOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Supermarkets Price Comparator API")
                .description(
                    "Backend API for comparing product prices across Spanish"
                        + " supermarkets (Mercadona, Carrefour, Alcampo,"
                        + " ALDI, LIDL, DIA).")
                .version("0.0.1-SNAPSHOT")
                .contact(new Contact().name("n1b3lung0").url("https://github.com/n1b3lung0"))
                .license(new License().name("MIT")));
  }
}
