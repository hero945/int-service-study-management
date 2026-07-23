package com.huadong.pipeline.domain.study;

import java.util.List;

/** 管线总览读模型的数据访问端口：按数据范围返回带 study 的 project（含 TA 信息）。 */
public interface PipelineOverviewRepository {
  List<OverviewProject> findOverviewProjects(StudyAccessScope scope);
}
