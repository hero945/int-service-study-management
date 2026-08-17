package com.huadong.pipeline.domain.study;

import java.time.LocalDateTime;

/**
 * Project 维度监管里程碑在管线总览中的展示状态。
 */
public record RegulatoryOverviewStatus(
    String mainStageCode,
    String mainStageLabel,
    String subStatusLabel,
    boolean preindCompleted,
    boolean indCompleted,
    boolean pre3Completed,
    boolean prendaCompleted,
    boolean ndaCompleted,
    LocalDateTime updatedAt) {
}
