package com.huadong.pipeline.domain.study;

public class InvalidStudyHierarchyException extends RuntimeException {
  public InvalidStudyHierarchyException() {
    super("Study parent hierarchy does not exist or is inconsistent");
  }
}
