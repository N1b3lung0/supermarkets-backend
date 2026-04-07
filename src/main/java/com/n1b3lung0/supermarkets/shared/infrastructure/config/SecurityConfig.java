package com.n1b3lung0.supermarkets.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless JWT security configuration.
 *
 * <p>Public (no auth required):
 *
 * <ul>
 *   <li>{@code GET /api/v1/products/**}
 *   <li>{@code GET /api/v1/categories/**}
 *   <li>{@code GET /api/v1/supermarkets/**}
 *   <li>{@code GET /api/v1/comparisons/**}
 *   <li>{@code /actuator/health}, {@code /actuator/info}
 *   <li>{@code /swagger-ui/**}, {@code /v3/api-docs/**}
 * </ul>
 *
 * <p>Everything else (baskets, sync trigger, sync-run query) requires a valid Bearer JWT.
 *
 * <p>{@code SecurityContextHolder} must never be accessed outside {@code infrastructure/}.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Read-only product catalogue — public
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/supermarkets/**")
                    .permitAll()
                    // Price comparison — public
                    .requestMatchers(HttpMethod.GET, "/api/v1/comparisons/**")
                    .permitAll()
                    // Ops / docs
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**")
                    .permitAll()
                    // Everything else (baskets, sync) requires auth
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .build();
  }
}
