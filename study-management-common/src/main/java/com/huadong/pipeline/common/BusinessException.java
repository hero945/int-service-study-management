package com.huadong.pipeline.common;

import java.util.Map;

public class BusinessException extends RuntimeException {
  private final String code;
  private final Map<String, String> details;

  public BusinessException(String code, String message) {
    this(code, message, Map.of());
  }

  public BusinessException(String code, String message, Map<String, String> details) {
    super(message);
    this.code = code;
    this.details = Map.copyOf(details);
  }

  public String code() {
    return code;
  }

  public Map<String, String> details() {
    return details;
  }
}
