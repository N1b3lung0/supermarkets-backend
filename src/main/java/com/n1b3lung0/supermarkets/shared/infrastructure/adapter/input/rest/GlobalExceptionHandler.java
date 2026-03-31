package com.n1b3lung0.supermarkets.shared.infrastructure.adapter.input.rest;

import com.n1b3lung0.supermarkets.shared.domain.exception.BusinessRuleViolationException;
import com.n1b3lung0.supermarkets.shared.domain.exception.ConflictException;
import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;
import com.n1b3lung0.supermarkets.shared.domain.exception.UnauthorizedException;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central exception handler. Catches exceptions by their base category — never by concrete type.
 * All responses use RFC 9457 {@link ProblemDetail}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String ERROR_TYPE_BASE = "https://api.supermarkets.n1b3lung0.com/errors/";

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, ex);
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ProblemDetail handleBusinessRule(BusinessRuleViolationException ex) {
    return problem(HttpStatus.UNPROCESSABLE_CONTENT, ex);
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflict(ConflictException ex) {
    return problem(HttpStatus.CONFLICT, ex);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
    return problem(HttpStatus.FORBIDDEN, ex);
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ProblemDetail handleExternalService(ExternalServiceException ex) {
    log.error("External service failure: {}", ex.getMessage(), ex);
    return problem(HttpStatus.BAD_GATEWAY, ex);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setType(URI.create(ERROR_TYPE_BASE + "missing-parameter"));
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    var problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Validation failed");
    problem.setType(URI.create(ERROR_TYPE_BASE + "validation"));
    problem.setProperty(
        "violations",
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
            .toList());
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    log.error("Unexpected error", ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, ex);
  }

  private ProblemDetail problem(HttpStatus status, Exception ex) {
    var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    var slug =
        ex.getClass().getSimpleName().toLowerCase().replace("exception", "").replace("_", "-");
    problem.setType(URI.create(ERROR_TYPE_BASE + slug));
    return problem;
  }
}
