package com.huadong.pipeline.common;

public enum StudyStatus {
  PLANNED("计划中", "neutral"),
  ACTIVE("进行中", "positive"),
  ON_HOLD("已暂停", "warning"),
  COMPLETED("已完成", "info");

  private final String label;
  private final String tone;

  StudyStatus(String label, String tone) {
    this.label = label;
    this.tone = tone;
  }

  public String label() {
    return label;
  }

  public String tone() {
    return tone;
  }
}
