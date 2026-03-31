package com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n1b3lung0.supermarkets.shared.domain.exception.BusinessRuleViolationException;
import com.n1b3lung0.supermarkets.shared.domain.exception.ConflictException;
import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;
import com.n1b3lung0.supermarkets.shared.domain.exception.UnauthorizedException;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verifies that {@link GlobalExceptionHandler} maps each exception category to the correct HTTP
 * status. Uses standalone MockMvc — no Spring context required (Spring Boot 4 removed @WebMvcTest).
 */
class GlobalExceptionHandlerTest {

  // --- Stub controller that throws each exception type on demand ---

  @RestController
  static class StubController {

    @GetMapping("/stub")
    public String handle(@RequestParam String type) {
      return switch (type) {
        case "not-found" -> throw new StubNotFoundException();
        case "business-rule" -> throw new StubBusinessRuleException();
        case "conflict" -> throw new StubConflictException();
        case "unauthorized" -> throw new StubUnauthorizedException();
        case "external" -> throw new ExternalServiceException("StubAPI", "timeout");
        default -> "ok";
      };
    }
  }

  static class StubNotFoundException extends NotFoundException {
    StubNotFoundException() {
      super("resource not found");
    }
  }

  static class StubBusinessRuleException extends BusinessRuleViolationException {
    StubBusinessRuleException() {
      super("rule violated");
    }
  }

  static class StubConflictException extends ConflictException {
    StubConflictException() {
      super("already exists");
    }
  }

  static class StubUnauthorizedException extends UnauthorizedException {
    StubUnauthorizedException() {
      super("access denied");
    }
  }

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new StubController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void notFoundException_shouldReturn404WithProblemDetail() throws Exception {
    // when / then
    mockMvc
        .perform(get("/stub").param("type", "not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("resource not found"));
  }

  @Test
  void businessRuleViolationException_shouldReturn422WithProblemDetail() throws Exception {
    mockMvc
        .perform(get("/stub").param("type", "business-rule"))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.detail").value("rule violated"));
  }

  @Test
  void conflictException_shouldReturn409WithProblemDetail() throws Exception {
    mockMvc
        .perform(get("/stub").param("type", "conflict"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value("already exists"));
  }

  @Test
  void unauthorizedException_shouldReturn403WithProblemDetail() throws Exception {
    mockMvc
        .perform(get("/stub").param("type", "unauthorized"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.detail").value("access denied"));
  }

  @Test
  void externalServiceException_shouldReturn502WithProblemDetail() throws Exception {
    mockMvc
        .perform(get("/stub").param("type", "external"))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.status").value(502));
  }
}
