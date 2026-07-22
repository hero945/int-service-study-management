package com.huadong.pipeline.domain.config;

import java.time.LocalDateTime;

public record RenameImpact(
    long projectCount,
    long studyCount,
    LocalDateTime expectedUpdatedAt) {
}
