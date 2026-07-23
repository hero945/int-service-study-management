package com.huadong.pipeline.repository.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 管线总览 study 查询行（含所属 projectId 与状态推断所需日期）。 */
public record OverviewStudyRow(
    long id,
    String code,
    long projectId,
    String phase,
    LocalDate startDate,
    LocalDate actualStartDate,
    LocalDate actualEndDate,
    LocalDateTime updatedAt) {
}
