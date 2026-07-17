package com.huadong.pipeline.domain.study;

public class DuplicateStudyCodeException extends RuntimeException {
  public DuplicateStudyCodeException(Throwable cause) {
    super(cause);
  }
}
