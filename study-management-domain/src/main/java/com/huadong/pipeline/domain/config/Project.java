package com.huadong.pipeline.domain.config;

import java.time.LocalDateTime;

public record Project(
    long id,
    int version,
    String code,
    long programId,
    String programCode,
    String indication,
    long therapeuticAreaId,
    String therapeuticAreaCode,
    String therapeuticAreaName,
    long studyCount,
    LocalDateTime updatedAt) {
}
