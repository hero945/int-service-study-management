package com.huadong.pipeline.domain.study;

public record StudyAccessScope(boolean allStudies, long userId) {
  public static StudyAccessScope all() {
    return new StudyAccessScope(true, 0);
  }

  public static StudyAccessScope assignedTo(long userId) {
    if (userId <= 0) {
      throw new IllegalArgumentException("userId must be positive for assigned Study scope");
    }
    return new StudyAccessScope(false, userId);
  }
}
