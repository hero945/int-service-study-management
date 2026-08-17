package com.huadong.pipeline.domain.milestone;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Repository port for {@code hd_plt_project_milestone}.
 */
public interface ProjectMilestonePort {

  // ────────── query ──────────

  /** Load persisted milestone rows for a single project. */
  List<ProjectMilestone> findByProjectId(long projectId);

  /** Load persisted milestone rows for many projects (single IN query, avoids N+1). */
  List<ProjectMilestone> findByProjectIds(List<Long> projectIds);

  /** Group persisted rows by project id. */
  Map<Long, List<ProjectMilestone>> findByProjectIdsGrouped(List<Long> projectIds);

  // ────────── mutation ──────────

  /** Upsert (INSERT … ON DUPLICATE KEY UPDATE) one milestone row. */
  ProjectMilestone save(ProjectMilestoneCommand command);

  record ProjectMilestoneCommand(
      long projectId,
      String stageCode,
      String milestoneCode,
      LocalDate planV1Date,
      LocalDate planV2Date,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      String deviationNote,
      String operatorEmail) {
  }
}
