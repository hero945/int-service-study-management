package com.huadong.pipeline.domain.study;

import com.huadong.pipeline.common.StudyStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 管线总览中一个 project 下的单个研究（用于 7 阶段单元格）。 */
public record OverviewStudy(
    long id,
    String code,
    String phase,
    StudyStatus status,
    LocalDate startDate,
    LocalDateTime updatedAt,
    /** 当前里程碑阶段编码 (e.g. "PreIND") */
    String mainStageCode,
    /** 当前里程碑阶段标签 (e.g. "PreIND") */
    String mainStageLabel,
    /** 已到达的节点标签 (e.g. "PreIND 递交" / "未开始") */
    String subStatusLabel,
    /** PreIND stage 最后节点 actual_end != null */
    boolean preindCompleted,
    /** IND stage 最后节点 actual_end != null */
    boolean indCompleted,
    /** 全局最末里程碑节点 actual_end != null */
    boolean globallyCompleted,
    /** 当前阶段对应里程碑最后节点 actual_end != null（当前阶段已完成） */
    boolean currentPhaseCompleted,
    /** 项目负责人姓名（可多名，顿号分隔） */
    String plName,
    /** 项目经理姓名（可多名，顿号分隔） */
    String pmName) {
}
