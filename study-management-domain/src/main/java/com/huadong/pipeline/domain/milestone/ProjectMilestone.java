package com.huadong.pipeline.domain.milestone;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Project 维度监管里程碑节点（PreIND/IND/Pre3/PreNDA_BLA/NDA_BLA）。
 * 同一个 project 下所有 study 共享这些节点。
 */
public record ProjectMilestone(
    long id,
    long projectId,
    String stageCode,
    String milestoneCode,
    LocalDate planV1Date,
    LocalDate planV2Date,
    LocalDate actualStartDate,
    LocalDate actualEndDate,
    String deviationNote,
    LocalDateTime updatedAt) {
}
