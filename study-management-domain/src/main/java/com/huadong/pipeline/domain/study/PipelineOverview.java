package com.huadong.pipeline.domain.study;

import java.util.List;

/** 管线总览读模型：按 TA 聚合 project，每个 project 带其 study（用于阶段单元格）。 */
public record PipelineOverview(
    String title,
    List<OverviewArea> areas) {
}
