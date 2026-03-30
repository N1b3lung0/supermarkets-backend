package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest.GlobalExceptionHandler;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncRunView;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import com.n1b3lung0.supermarkets.sync.application.port.output.SyncRunQueryPort;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Step 59 — MockMvc tests for SyncController. */
@ExtendWith(MockitoExtension.class)
class SyncControllerTest {

  @Mock private SyncSupermarketCatalogUseCase syncUseCase;
  @Mock private SyncRunQueryPort syncRunQueryPort;

  private MockMvc mockMvc;

  private static final UUID SUPERMARKET_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  @BeforeEach
  void setUp() {
    var controller = new SyncController(syncUseCase, syncRunQueryPort);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  void triggerSync_shouldReturn202() throws Exception {
    given(syncUseCase.execute(any())).willReturn(SyncRunId.generate());

    mockMvc
        .perform(post("/api/v1/sync/supermarkets/{id}", SUPERMARKET_ID))
        .andExpect(status().isAccepted());

    verify(syncUseCase).execute(any());
  }

  @Test
  void listRuns_shouldReturnPagedResults() throws Exception {
    var view =
        new SyncRunView(
            UUID.randomUUID(),
            SUPERMARKET_ID,
            Instant.now(),
            Instant.now(),
            "COMPLETED",
            10,
            500,
            2,
            null);
    given(syncRunQueryPort.findBySupermarketId(any(), any()))
        .willReturn(new PageImpl<>(List.of(view), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/api/v1/sync/runs").param("supermarketId", SUPERMARKET_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$.content[0].productsSynced").value(500))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void listRuns_missingParam_shouldReturn400() throws Exception {
    mockMvc.perform(get("/api/v1/sync/runs")).andExpect(status().isBadRequest());
  }
}
