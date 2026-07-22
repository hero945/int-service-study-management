package com.huadong.pipeline.repository.mapper;

import java.time.LocalDateTime;

public record ProgramSummaryData(
    long id,
    String code,
    String name,
    String productName,
    String moa,
    String sourceCode,
    String originCode,
    long projectCount,
    long studyCount,
    LocalDateTime updatedAt) {
}
