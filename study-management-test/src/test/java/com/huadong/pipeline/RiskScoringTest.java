package com.huadong.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.huadong.pipeline.domain.risk.RiskLevel;
import org.junit.jupiter.api.Test;

class RiskScoringTest {
  @Test
  void appliesVersionOneThresholdBoundaries() {
    assertEquals(RiskLevel.LOW, RiskLevel.fromScore(12, 12, 36));
    assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(13, 12, 36));
    assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(36, 12, 36));
    assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(37, 12, 36));
  }
}
