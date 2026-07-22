package com.huadong.pipeline.domain.config;

import java.time.LocalDateTime;

public record Program(
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
