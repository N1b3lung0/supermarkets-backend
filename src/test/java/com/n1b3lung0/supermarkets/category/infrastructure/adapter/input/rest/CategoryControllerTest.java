package com.n1b3lung0.supermarkets.category.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.port.input.command.RegisterCategoryUseCase;
import com.n1b3lung0.supermarkets.category.application.port.input.query.GetCategoryByIdUseCase;
import com.n1b3lung0.supermarkets.category.application.port.input.query.ListCategoriesUseCase;
import com.n1b3lung0.supermarkets.category.domain.exception.CategoryNotFoundException;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest.GlobalExceptionHandler;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

  @Mock private RegisterCategoryUseCase registerUseCase;
  @Mock private GetCategoryByIdUseCase getByIdUseCase;
  @Mock private ListCategoriesUseCase listUseCase;

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new CategoryController(registerUseCase, getByIdUseCase, listUseCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void register_shouldReturn201WithLocation_whenValidRequest() throws Exception {
    var newId = CategoryId.generate();
    given(registerUseCase.execute(any())).willReturn(newId);
    var body =
        objectMapper.writeValueAsString(
            Map.of(
                "name", "Frescos",
                "externalId", "10",
                "supermarketId", UUID.randomUUID().toString(),
                "levelType", "TOP",
                "order", 0));

    mockMvc
        .perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/categories/" + newId));
  }

  @Test
  void register_shouldReturn422_whenNameIsBlank() throws Exception {
    var body =
        objectMapper.writeValueAsString(
            Map.of(
                "name", "",
                "externalId", "10",
                "supermarketId", UUID.randomUUID().toString(),
                "levelType", "TOP",
                "order", 0));

    mockMvc
        .perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void register_shouldReturn422_whenLevelTypeIsInvalid() throws Exception {
    var body =
        objectMapper.writeValueAsString(
            Map.of(
                "name", "Frescos",
                "externalId", "10",
                "supermarketId", UUID.randomUUID().toString(),
                "levelType", "INVALID",
                "order", 0));

    mockMvc
        .perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void getById_shouldReturn200_whenFound() throws Exception {
    var id = UUID.randomUUID();
    var view =
        new CategoryDetailView(
            id, "Frescos", "10", UUID.randomUUID(), "TOP", null, 0, Instant.now());
    given(getByIdUseCase.execute(any())).willReturn(view);

    mockMvc
        .perform(get("/api/v1/categories/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Frescos"))
        .andExpect(jsonPath("$.levelType").value("TOP"));
  }

  @Test
  void getById_shouldReturn404_whenNotFound() throws Exception {
    var id = UUID.randomUUID();
    given(getByIdUseCase.execute(any()))
        .willThrow(new CategoryNotFoundException(CategoryId.of(id)));

    mockMvc.perform(get("/api/v1/categories/{id}", id)).andExpect(status().isNotFound());
  }
}
