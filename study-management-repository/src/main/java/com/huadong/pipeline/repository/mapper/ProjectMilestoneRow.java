package com.huadong.pipeline.repository.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** project 维度监管里程碑查询行。 */
public record ProjectMilestoneRow(
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
