package com.huadong.pipeline.domain.risk;

public enum RiskLevel {
  LOW, MEDIUM, HIGH;

  public static RiskLevel fromScore(int score, int lowMax, int mediumMax) {
    if (score < 1 || score > 125 || lowMax < 1 || mediumMax < lowMax) {
      throw new IllegalArgumentException("风险评分或阈值不合法");
    }
    if (score <= lowMax) return LOW;
    if (score <= mediumMax) return MEDIUM;
    return HIGH;
  }
}
