package com.huadong.pipeline.repository.mapper;

import java.time.LocalDateTime;

public record PipelineConfigRowData(
    long studyId,
    String studyCode,
    String phaseStatusCode,
    long projectId,
    String projectCode,
    String indication,
    String therapeuticAreaCode,
    String therapeuticAreaName,
    long programId,
    String programCode,
    String productName,
    String moa,
    String sourceCode,
    String originCode,
    LocalDateTime updatedAt) {
}
