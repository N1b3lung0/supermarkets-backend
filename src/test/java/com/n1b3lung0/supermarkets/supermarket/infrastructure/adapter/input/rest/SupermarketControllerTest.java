package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest.GlobalExceptionHandler;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.command.RegisterSupermarketUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.GetSupermarketByIdUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.ListSupermarketsUseCase;
import com.n1b3lung0.supermarkets.supermarket.domain.exception.SupermarketNotFoundException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.input.rest.dto.RegisterSupermarketRequest;
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
class SupermarketControllerTest {

  @Mock private RegisterSupermarketUseCase registerUseCase;

  @Mock private GetSupermarketByIdUseCase getByIdUseCase;

  @Mock private ListSupermarketsUseCase listUseCase;

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new SupermarketController(registerUseCase, getByIdUseCase, listUseCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void register_shouldReturn201WithLocation_whenValidRequest() throws Exception {
    // given
    var newId = SupermarketId.generate();
    given(registerUseCase.execute(any())).willReturn(newId);
    var body = objectMapper.writeValueAsString(Map.of("name", "Mercadona", "country", "ES"));

    // when / then
    mockMvc
        .perform(post("/api/v1/supermarkets").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/supermarkets/" + newId));
  }

  @Test
  void register_shouldReturn422_whenNameIsBlank() throws Exception {
    // given
    var body = objectMapper.writeValueAsString(new RegisterSupermarketRequest("", "ES"));

    // when / then
    mockMvc
        .perform(post("/api/v1/supermarkets").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnprocessableContent());
  }

  @Test
  void getById_shouldReturn200WithDetail_whenFound() throws Exception {
    // given
    var id = UUID.randomUUID();
    var view = new SupermarketDetailView(id, "Mercadona", "ES", Instant.now());
    given(getByIdUseCase.execute(any())).willReturn(view);

    // when / then
    mockMvc
        .perform(get("/api/v1/supermarkets/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Mercadona"))
        .andExpect(jsonPath("$.country").value("ES"));
  }

  @Test
  void getById_shouldReturn404_whenNotFound() throws Exception {
    // given
    var id = UUID.randomUUID();
    given(getByIdUseCase.execute(any()))
        .willThrow(new SupermarketNotFoundException(SupermarketId.of(id)));

    // when / then
    mockMvc
        .perform(get("/api/v1/supermarkets/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }
}
