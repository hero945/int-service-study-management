package com.huadong.pipeline.web;

import lombok.extern.slf4j.Slf4j;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.audit.AuditFailureRecorder;
import io.sentry.Sentry;
import io.sentry.protocol.User;
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
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
  private final AuditFailureRecorder auditFailures;

  public ApiExceptionHandler(AuditFailureRecorder auditFailures) {
    this.auditFailures = auditFailures;
  }

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiError> business(BusinessException ex, HttpServletRequest request) {
    HttpStatus status = resolveBusinessStatus(ex);
    auditFailures.record(
        request, status == HttpStatus.FORBIDDEN ? "DENIED" : "FAILED",
        ex.code(), ex.getMessage());
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

  private static HttpStatus resolveBusinessStatus(BusinessException ex) {
    if (ex.httpStatus() > 0) {
      return HttpStatus.valueOf(ex.httpStatus());
    }
    return switch (ex.code()) {
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
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> accessDenied(AccessDeniedException ex, HttpServletRequest request) {
    auditFailures.record(request, "DENIED", "ACCESS_DENIED", "权限不足");
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
  ResponseEntity<ApiError> validation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    auditFailures.record(request, "FAILED", "VALIDATION_FAILED", "字段校验失败");
    var fieldErrors = ex.getBindingResult().getFieldErrors();
    String message;
    Map<String, String> details;
    if (!fieldErrors.isEmpty()) {
      FieldError first = fieldErrors.getFirst();
      message = "字段校验失败";
      details = Map.of("field", first.getField(), "reason", String.valueOf(first.getDefaultMessage()));
    } else {
      var globalErrors = ex.getBindingResult().getGlobalErrors();
      if (!globalErrors.isEmpty()) {
        var first = globalErrors.getFirst();
        message = "对象校验失败";
        details = Map.of("object", first.getObjectName(), "reason", String.valueOf(first.getDefaultMessage()));
      } else {
        message = "请求参数校验失败";
        details = Map.of();
      }
    }
    Sentry.withScope(
            scope -> {
//              scope.setTag("http.method", request.getMethod());
//              scope.setTag("http.path", request.getRequestURI());
              User user = new User();
              user.setUsername(currentUsername());
              scope.setUser(user);
              Sentry.captureException(ex);
            });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiError("VALIDATION_FAILED", message, details, Instant.now()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiError> unreadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    auditFailures.record(request, "FAILED", "INVALID_REQUEST_BODY", "请求内容格式不正确");
    return ResponseEntity.badRequest()
        .body(new ApiError("INVALID_REQUEST_BODY", "请求内容格式不正确", Map.of(), Instant.now()));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ResponseEntity<ApiError> missingParameter(MissingServletRequestParameterException ex) {
    return ResponseEntity.badRequest()
        .body(new ApiError("MISSING_PARAMETER",
            "缺少必填参数: " + ex.getParameterName(), Map.of(), Instant.now()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex) {
    return ResponseEntity.badRequest()
        .body(new ApiError("INVALID_PARAMETER_TYPE",
            "参数类型错误: " + ex.getName(), Map.of(), Instant.now()));
  }

  @ExceptionHandler(MissingPathVariableException.class)
  ResponseEntity<ApiError> missingPathVariable(MissingPathVariableException ex) {
    return ResponseEntity.badRequest()
        .body(new ApiError("MISSING_PATH_VARIABLE",
            "缺少路径参数: " + ex.getVariableName(), Map.of(), Instant.now()));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ApiError> methodNotSupported(HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new ApiError("METHOD_NOT_ALLOWED",
            "不支持的请求方法: " + ex.getMethod(), Map.of(), Instant.now()));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  ResponseEntity<ApiError> notFound(NoHandlerFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError("NOT_FOUND", "请求路径不存在", Map.of(), Instant.now()));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  ResponseEntity<ApiError> mediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(new ApiError("UNSUPPORTED_MEDIA_TYPE",
            "不支持的媒体类型: " + ex.getContentType(), Map.of(), Instant.now()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
    auditFailures.record(request, "FAILED", "INTERNAL_ERROR", "服务器内部错误");
    log.error(
        "未捕获异常 user={} method={} path={}",
        currentUsername(),
        request.getMethod(),
        request.getRequestURI(),
        ex);
    // @ExceptionHandler 已吞掉异常，需手动上报；业务/校验异常不走此分支
    Sentry.withScope(
        scope -> {
          scope.setTag("http.method", request.getMethod());
          scope.setTag("http.path", request.getRequestURI());
          User user = new User();
          user.setUsername(currentUsername());
          scope.setUser(user);
          Sentry.captureException(ex);
        });
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
