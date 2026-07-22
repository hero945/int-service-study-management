package com.huadong.pipeline.domain.config;

public record StudyReferenceCounts(long team, long milestone, long monthlyReport, long risk) {
  public long total() {
    return team + milestone + monthlyReport + risk;
  }
}
