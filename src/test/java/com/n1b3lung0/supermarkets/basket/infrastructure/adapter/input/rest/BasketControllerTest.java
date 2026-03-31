package com.n1b3lung0.supermarkets.basket.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1b3lung0.supermarkets.basket.application.dto.BasketComparisonView;
import com.n1b3lung0.supermarkets.basket.application.dto.BasketDetailView;
import com.n1b3lung0.supermarkets.basket.application.dto.BasketItemView;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.AddBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.ClearBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.CreateBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.RemoveBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.UpdateBasketItemQuantityUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.query.CompareBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.input.query.GetBasketByIdUseCase;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;
import com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest.GlobalExceptionHandler;
import java.util.List;
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

/** MockMvc tests for BasketController. */
@ExtendWith(MockitoExtension.class)
class BasketControllerTest {

  @Mock private CreateBasketUseCase createBasket;
  @Mock private AddBasketItemUseCase addItem;
  @Mock private RemoveBasketItemUseCase removeItem;
  @Mock private UpdateBasketItemQuantityUseCase updateQuantity;
  @Mock private ClearBasketUseCase clearBasket;
  @Mock private GetBasketByIdUseCase getById;
  @Mock private CompareBasketUseCase compareBasket;

  private MockMvc mockMvc;
  private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

  private static final UUID BASKET_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
  private static final UUID ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new BasketController(
                    createBasket,
                    addItem,
                    removeItem,
                    updateQuantity,
                    clearBasket,
                    getById,
                    compareBasket))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void createBasket_returns201WithLocation() throws Exception {
    given(createBasket.execute(any())).willReturn(BasketId.of(BASKET_ID));

    mockMvc
        .perform(
            post("/api/v1/baskets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("name", "Mi cesta"))))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/baskets/" + BASKET_ID));
  }

  @Test
  void createBasket_missingName_returns422() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/baskets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("name", ""))))
        .andExpect(status().isUnprocessableContent());
  }

  @Test
  void getById_found_returns200() throws Exception {
    var view =
        new BasketDetailView(
            BASKET_ID, "Mi cesta", List.of(new BasketItemView(ITEM_ID, "Leche 1L", 2)));
    given(getById.execute(any())).willReturn(view);

    mockMvc
        .perform(get("/api/v1/baskets/{id}", BASKET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Mi cesta"))
        .andExpect(jsonPath("$.items[0].productName").value("Leche 1L"))
        .andExpect(jsonPath("$.items[0].quantity").value(2));
  }

  @Test
  void getById_notFound_returns404() throws Exception {
    given(getById.execute(any())).willThrow(new BasketNotFoundException(BASKET_ID.toString()));

    mockMvc.perform(get("/api/v1/baskets/{id}", BASKET_ID)).andExpect(status().isNotFound());
  }

  @Test
  void addItem_returns201WithLocation() throws Exception {
    given(addItem.execute(any())).willReturn(BasketItemId.of(ITEM_ID));

    mockMvc
        .perform(
            post("/api/v1/baskets/{id}/items", BASKET_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("productName", "Leche 1L", "quantity", 2))))
        .andExpect(status().isCreated())
        .andExpect(
            header().string("Location", "/api/v1/baskets/" + BASKET_ID + "/items/" + ITEM_ID));
  }

  @Test
  void updateQuantity_returns204() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/baskets/{id}/items/{itemId}", BASKET_ID, ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("quantity", 3))))
        .andExpect(status().isNoContent());
    verify(updateQuantity).execute(any());
  }

  @Test
  void removeItem_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/baskets/{id}/items/{itemId}", BASKET_ID, ITEM_ID))
        .andExpect(status().isNoContent());
    verify(removeItem).execute(any());
  }

  @Test
  void clearBasket_returns204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/baskets/{id}/items", BASKET_ID))
        .andExpect(status().isNoContent());
    verify(clearBasket).execute(any());
  }

  @Test
  void compare_returns200WithComparisonView() throws Exception {
    var view = new BasketComparisonView(BASKET_ID, "Mi cesta", List.of(), null, null);
    given(compareBasket.execute(any())).willReturn(view);

    mockMvc
        .perform(get("/api/v1/baskets/{id}/compare", BASKET_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.basketName").value("Mi cesta"))
        .andExpect(jsonPath("$.perSupermarket").isArray());
  }
}
