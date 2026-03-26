package com.n1b3lung0.supermarkets.product.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductByIdUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsByCategoryUseCase;
import com.n1b3lung0.supermarkets.product.application.port.input.query.ListProductsBySupermarketUseCase;
import com.n1b3lung0.supermarkets.product.domain.ProductMother;
import com.n1b3lung0.supermarkets.product.domain.exception.ProductNotFoundException;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock private GetProductByIdUseCase getByIdUseCase;
  @Mock private ListProductsByCategoryUseCase listByCategoryUseCase;
  @Mock private ListProductsBySupermarketUseCase listBySupermarketUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new ProductController(
                    getByIdUseCase, listByCategoryUseCase, listBySupermarketUseCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  private ProductDetailView sampleView(UUID id) {
    return new ProductDetailView(
        id,
        "3400",
        ProductMother.DEFAULT_SUPERMARKET.value(),
        ProductMother.DEFAULT_CATEGORY.value(),
        "Leche Entera",
        null,
        null,
        "Hacendado",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        List.of(),
        false,
        false,
        false,
        false,
        true,
        0,
        Instant.now(),
        Instant.now(),
        null);
  }

  @Test
  void getById_shouldReturn200_whenFound() throws Exception {
    var id = UUID.randomUUID();
    given(getByIdUseCase.execute(any())).willReturn(sampleView(id));

    mockMvc
        .perform(get("/api/v1/products/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Leche Entera"))
        .andExpect(jsonPath("$.brand").value("Hacendado"));
  }

  @Test
  void getById_shouldReturn404_whenNotFound() throws Exception {
    given(getByIdUseCase.execute(any()))
        .willThrow(new ProductNotFoundException(ProductId.generate()));

    mockMvc
        .perform(get("/api/v1/products/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  @Test
  void listBySupermarket_shouldReturn200() throws Exception {
    given(listBySupermarketUseCase.execute(any())).willReturn(new PageImpl<>(List.of()));

    mockMvc
        .perform(
            get(
                "/api/v1/supermarkets/{supermarketId}/products",
                ProductMother.DEFAULT_SUPERMARKET.value()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void listByCategory_shouldReturn200() throws Exception {
    given(listByCategoryUseCase.execute(any())).willReturn(new PageImpl<>(List.of()));

    mockMvc
        .perform(
            get("/api/v1/categories/{categoryId}/products", ProductMother.DEFAULT_CATEGORY.value()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }
}
