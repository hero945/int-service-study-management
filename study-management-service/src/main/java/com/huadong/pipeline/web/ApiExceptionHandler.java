package com.huadong.pipeline.web;

import lombok.extern.slf4j.Slf4j;

import com.huadong.pipeline.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiError> business(BusinessException ex, HttpServletRequest request) {
    HttpStatus status = switch (ex.code()) {
      case "ROLE_NOT_FOUND", "PROGRAM_NOT_FOUND", "PROJECT_NOT_FOUND", "STUDY_NOT_FOUND",
          "RISK_NOT_FOUND", "RISK_ACTION_NOT_FOUND",
          "MONTHLY_REPORT_NOT_FOUND", "MONTHLY_ENTRY_NOT_FOUND", "USER_NOT_FOUND" ->
          HttpStatus.NOT_FOUND;
      case "ROLE_CODE_EXISTS", "SYSTEM_ROLE_PROTECTED", "ROLE_IN_USE",
          "LAST_ROLE_ADMIN_PROTECTED", "PROGRAM_CODE_EXISTS", "PRODUCT_NAME_EXISTS",
          "PROJECT_CODE_EXISTS", "STUDY_CODE_EXISTS", "PROGRAM_IN_USE", "PROJECT_IN_USE",
          "STUDY_IN_USE", "RENAME_CONFIRMATION_REQUIRED", "RENAME_IMPACT_CHANGED",
          "TEAM_VERSION_CONFLICT", "RISK_VERSION_CONFLICT", "USERNAME_EXISTS" ->
          HttpStatus.CONFLICT;
      case String code when code.endsWith("_FORBIDDEN") || code.endsWith("_OUT_OF_SCOPE") ->
          HttpStatus.FORBIDDEN;
      case "INVALID_PERMISSION", "INVALID_THERAPEUTIC_AREA", "INVALID_CONFIG_ENUM",
          "INVALID_TEAM_ROLE", "INVALID_TEAM_MEMBER", "INVALID_RISK", "MONTHLY_INVALID",
          "INVALID_ROLE" ->
          HttpStatus.UNPROCESSABLE_ENTITY;
      default -> HttpStatus.BAD_REQUEST;
    };
    if (status == HttpStatus.FORBIDDEN) {
      log.warn(
          "业务禁止访问 code={} user={} method={} path={} message={}",
          ex.code(),
          currentUsername(),
          request.getMethod(),
          request.getRequestURI(),
          ex.getMessage());
    }
    return ResponseEntity.status(status)
        .body(new ApiError(ex.code(), ex.getMessage(), ex.details(), Instant.now()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> accessDenied(AccessDeniedException ex, HttpServletRequest request) {
    log.warn(
        "访问被拒绝 user={} method={} path={}",
        currentUsername(),
        request.getMethod(),
        request.getRequestURI());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ApiError("ACCESS_DENIED", "无权执行此操作", Map.of(), Instant.now()));
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ApiError> authentication(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiError("UNAUTHENTICATED", "请先登录", Map.of(), Instant.now()));
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

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
    log.error(
        "未捕获异常 user={} method={} path={}",
        currentUsername(),
        request.getMethod(),
        request.getRequestURI(),
        ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("INTERNAL_ERROR", "服务器内部错误", Map.of(), Instant.now()));
  }

  private static String currentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return "anonymous";
    }
    String name = authentication.getName();
    return name == null || name.isBlank() ? "anonymous" : name;
  }

  record ApiError(String code, String message, Map<String, String> details, Instant timestamp) {
  }
}
