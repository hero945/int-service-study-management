package com.huadong.pipeline.repository.mapper;

public class UserStudyCountRow {
  private long userId;
  private long studyCount;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public long getStudyCount() {
    return studyCount;
  }

  public void setStudyCount(long studyCount) {
    this.studyCount = studyCount;
  }
}
