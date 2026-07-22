package com.huadong.pipeline.repository.mapper;

import java.time.LocalDateTime;

public record ProjectSummaryData(
    long id,
    String code,
    String name,
    long programId,
    String programCode,
    String indication,
    long therapeuticAreaId,
    String therapeuticAreaCode,
    String therapeuticAreaName,
    long studyCount,
    LocalDateTime updatedAt) {
}
