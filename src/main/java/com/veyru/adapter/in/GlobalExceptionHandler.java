package com.veyru.adapter.in;

import com.veyru.application.identity.IdentityException;
import com.veyru.application.messaging.MessagingException;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.adapter.in.error.ErrorCode;
import com.veyru.adapter.in.error.ValidationError;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(UseCaseException.class)
  public ProblemDetail handleUseCaseException(UseCaseException ex, WebRequest request) {
    return problem(ErrorCode.valueOf(ex.code().name()), ex.getMessage(), request);
  }

  @ExceptionHandler(MessagingException.class)
  public ProblemDetail handleMessagingException(MessagingException ex, WebRequest request) {
    ErrorCode code =
        ex.reason() == MessagingException.Reason.ACCESS_DENIED
            ? ErrorCode.ACCESS_DENIED
            : ErrorCode.RESOURCE_NOT_FOUND;
    return problem(code, null, request);
  }

  @ExceptionHandler(IdentityException.class)
  public ProblemDetail handleIdentityException(IdentityException ex, WebRequest request) {
    ErrorCode code =
        ex.reason() == IdentityException.Reason.CONFLICT
            ? ErrorCode.RESOURCE_CONFLICT
            : ErrorCode.VALIDATION_FAILED;
    return problem(code, null, request);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ProblemDetail handleNotFound(Exception ex, WebRequest request) {
    return problem(ErrorCode.RESOURCE_NOT_FOUND, null, request);
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ProblemDetail handleConflict(Exception ex, WebRequest request) {
    return problem(ErrorCode.RESOURCE_CONFLICT, null, request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
    List<ValidationError> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    new ValidationError(
                        error.getField(),
                        constraintCode(error.getCode()),
                        error.getDefaultMessage()))
            .toList();
    ProblemDetail problem = problem(ErrorCode.VALIDATION_FAILED, null, request);
    problem.setProperty("errors", errors);
    return problem;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    List<ValidationError> errors =
        ex.getConstraintViolations().stream()
            .map(
                error ->
                    new ValidationError(
                        error.getPropertyPath().toString(),
                        constraintCode(
                            error
                                .getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                .getSimpleName()),
                        error.getMessage()))
            .toList();
    ProblemDetail problem = problem(ErrorCode.VALIDATION_FAILED, null, request);
    problem.setProperty("errors", errors);
    return problem;
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MissingServletRequestPartException.class
  })
  public ProblemDetail handleMalformedRequest(Exception ex, WebRequest request) {
    return problem(
        ex instanceof MissingServletRequestParameterException
                || ex instanceof MissingServletRequestPartException
            ? ErrorCode.MISSING_REQUEST_VALUE
            : ErrorCode.MALFORMED_REQUEST,
        null,
        request);
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
  public ProblemDetail handleInvalidValue(Exception ex, WebRequest request) {
    return problem(
        ex instanceof IllegalArgumentException
            ? ErrorCode.VALIDATION_FAILED
            : ErrorCode.INVALID_REQUEST_VALUE,
        null,
        request);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex, WebRequest request) {
    return problem(ErrorCode.METHOD_NOT_ALLOWED, null, request);
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ProblemDetail handleNotAcceptable(
      HttpMediaTypeNotAcceptableException ex, WebRequest request) {
    return problem(ErrorCode.NOT_ACCEPTABLE, null, request);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ProblemDetail handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException ex, WebRequest request) {
    return problem(ErrorCode.UNSUPPORTED_MEDIA_TYPE, null, request);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ProblemDetail handlePayloadTooLarge(
      MaxUploadSizeExceededException ex, WebRequest request) {
    return problem(ErrorCode.PAYLOAD_TOO_LARGE, null, request);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException ex, WebRequest request) {
    return problem(ErrorCode.INVALID_CREDENTIALS, null, request);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthentication(AuthenticationException ex, WebRequest request) {
    return problem(ErrorCode.AUTHENTICATION_REQUIRED, null, request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    return problem(ErrorCode.ACCESS_DENIED, null, request);
  }

  @ExceptionHandler(DataAccessResourceFailureException.class)
  public ProblemDetail handleServiceUnavailable(
      DataAccessResourceFailureException ex, WebRequest request) {
    log.error("Database dependency unavailable", ex);
    return problem(ErrorCode.SERVICE_UNAVAILABLE, null, request);
  }

  @ExceptionHandler(DataAccessException.class)
  public ProblemDetail handleDataAccess(DataAccessException ex, WebRequest request) {
    log.error("Database error", ex);
    return problem(ErrorCode.INTERNAL_ERROR, null, request);
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex, WebRequest request) {
    log.error("Unexpected error", ex);
    return problem(ErrorCode.INTERNAL_ERROR, null, request);
  }

  private ProblemDetail problem(ErrorCode code, String detail, WebRequest request) {
    String description = request.getDescription(false);
    URI instance = description.startsWith("uri=") ? URI.create(description.substring(4)) : null;
    var status = HttpErrorMapper.status(code);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(status, detail == null ? code.detail() : detail);
    problem.setTitle(status.getReasonPhrase());
    problem.setInstance(instance);
    problem.setProperty("code", code.name());
    return problem;
  }

  private String constraintCode(String code) {
    return switch (code) {
      case "NotBlank", "NotEmpty", "NotNull" -> "REQUIRED";
      case "Email" -> "INVALID_EMAIL";
      case "Size" -> "INVALID_SIZE";
      case "Pattern" -> "INVALID_FORMAT";
      default -> "INVALID_VALUE";
    };
  }
}
