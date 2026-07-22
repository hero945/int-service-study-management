package com.huadong.pipeline.domain.config;

import java.time.LocalDateTime;

public record PipelineConfigRow(
    long studyId,
    String studyCode,
    String studyName,
    String phaseStatusCode,
    long projectId,
    String projectCode,
    String projectName,
    String indication,
    String therapeuticAreaCode,
    String therapeuticAreaName,
    long programId,
    String programCode,
    String programName,
    String productName,
    String moa,
    String sourceCode,
    String originCode,
    LocalDateTime updatedAt) {
}
