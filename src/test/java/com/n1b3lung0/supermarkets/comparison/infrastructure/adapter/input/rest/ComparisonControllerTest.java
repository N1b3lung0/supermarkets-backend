package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n1b3lung0.supermarkets.comparison.application.dto.ProductComparisonView;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductMatchView;
import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Step 66 — MockMvc tests for ComparisonController. */
@ExtendWith(MockitoExtension.class)
class ComparisonControllerTest {

  @Mock private CompareProductsByNameUseCase compareUseCase;

  private MockMvc mockMvc;

  private static final UUID SUPERMARKET_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ComparisonController(compareUseCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void compare_happyPath_returnsOkWithSortedMatches() throws Exception {
    var match =
        new ProductMatchView(
            UUID.randomUUID(),
            SUPERMARKET_ID,
            "Mercadona",
            "Leche entera 1L",
            BigDecimal.valueOf(0.85),
            null,
            null,
            null,
            Instant.now());
    var view = new ProductComparisonView("leche", List.of(match), SUPERMARKET_ID, "Mercadona");
    given(compareUseCase.execute(any())).willReturn(view);

    mockMvc
        .perform(get("/api/v1/compare").param("q", "leche"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.searchTerm").value("leche"))
        .andExpect(jsonPath("$.matches[0].productName").value("Leche entera 1L"))
        .andExpect(jsonPath("$.cheapestSupermarketName").value("Mercadona"));
  }

  @Test
  void compare_noMatches_returnsOkWithEmptyList() throws Exception {
    given(compareUseCase.execute(any()))
        .willReturn(new ProductComparisonView("xyz", List.of(), null, null));

    mockMvc
        .perform(get("/api/v1/compare").param("q", "xyz"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.matches").isEmpty());
  }

  @Test
  void compare_missingQ_returns400() throws Exception {
    mockMvc.perform(get("/api/v1/compare")).andExpect(status().isBadRequest());
  }
}
