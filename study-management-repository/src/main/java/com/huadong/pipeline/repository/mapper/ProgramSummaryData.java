package com.huadong.pipeline.repository.mapper;

import java.time.LocalDateTime;

public record ProgramSummaryData(
    long id,
    int version,
    String code,
    String productName,
    String moa,
    String sourceCode,
    String originCode,
    long projectCount,
    long studyCount,
    LocalDateTime updatedAt) {
}
