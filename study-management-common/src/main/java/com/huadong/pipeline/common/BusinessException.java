package com.huadong.pipeline.common;

import java.util.Map;

public class BusinessException extends RuntimeException {
  private final String code;
  private final Map<String, String> details;
  private final int httpStatus;

  public BusinessException(String code, String message) {
    this(code, message, Map.of(), 0);
  }

  public BusinessException(String code, String message, Map<String, String> details) {
    this(code, message, details, 0);
  }

  public BusinessException(String code, String message, int httpStatus) {
    this(code, message, Map.of(), httpStatus);
  }

  public BusinessException(String code, String message, Map<String, String> details, int httpStatus) {
    super(message);
    this.code = code;
    this.details = Map.copyOf(details);
    this.httpStatus = httpStatus;
  }

  public String code() {
    return code;
  }

  public Map<String, String> details() {
    return details;
  }

  /**
   * Explicit HTTP status code for this business error.
   * A value of 0 means "not specified"; callers should fall back to their own mapping.
   */
  public int httpStatus() {
    return httpStatus;
  }
}
