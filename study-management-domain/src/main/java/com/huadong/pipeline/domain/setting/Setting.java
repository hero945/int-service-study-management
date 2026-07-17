package com.huadong.pipeline.domain.setting;

import java.time.LocalDateTime;

public record Setting(
    String configKey,
    String configValue,
    String description,
    boolean publicVisible,
    String updatedBy,
    LocalDateTime updatedAt) {
}
