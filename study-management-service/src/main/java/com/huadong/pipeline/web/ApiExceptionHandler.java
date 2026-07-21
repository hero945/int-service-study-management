package com.huadong.pipeline.web;

import com.huadong.pipeline.common.BusinessException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiError> business(BusinessException ex) {
    HttpStatus status = switch (ex.code()) {
      case "ROLE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
      case "ROLE_CODE_EXISTS", "SYSTEM_ROLE_PROTECTED", "ROLE_IN_USE",
          "LAST_ROLE_ADMIN_PROTECTED" -> HttpStatus.CONFLICT;
      case "INVALID_PERMISSION" -> HttpStatus.UNPROCESSABLE_ENTITY;
      default -> HttpStatus.BAD_REQUEST;
    };
    return ResponseEntity.status(status)
        .body(new ApiError(ex.code(), ex.getMessage(), Map.of(), Instant.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
    FieldError first = ex.getBindingResult().getFieldErrors().getFirst();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiError(
            "VALIDATION_FAILED",
            "字段校验失败",
            Map.of("field", first.getField(), "reason", first.getDefaultMessage()),
            Instant.now()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest()
        .body(new ApiError("INVALID_REQUEST_BODY", "请求内容格式不正确", Map.of(), Instant.now()));
  }

  record ApiError(String code, String message, Map<String, String> details, Instant timestamp) {
  }
}
