package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public interface ProjectMilestoneApi {

  /** GET /api/v1/studies/{studyId}/project-milestones */
  ProjectMilestonePageResponse getProjectMilestones(long studyId, String username);

  /** PUT /api/v1/studies/{studyId}/project-milestones/{milestoneCode} */
  ProjectMilestonePageResponse updateProjectMilestone(long studyId, String milestoneCode,
      @Valid ProjectMilestoneUpdateRequest request, String username);

  /** GET /api/v1/studies/{studyId}/project-milestones/stage-projection */
  StageProjectionResponse getProjectStageProjection(long studyId, String username);

  // ──────────── response records ────────────

  record MilestoneNodeResponse(
      Long milestoneId,
      String milestoneCode,
      String milestoneName,
      LocalDate planV1Date,
      LocalDate planV2Date,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      String status,
      String deviationNote
  ) {}

  record StageGroupResponse(
      String stageCode,
      String stageName,
      List<MilestoneNodeResponse> nodes
  ) {}

  record ProjectMilestonePageResponse(
      String projectCode,
      List<StageGroupResponse> groups
  ) {}

  record StageProjectionResponse(
      String currentStageCode,
      String currentStageName,
      String currentMilestoneCode,
      String currentMilestoneName,
      String statusText
  ) {}

  // ──────────── request records ────────────

  record ProjectMilestoneUpdateRequest(
      LocalDate planV1Date,
      LocalDate planV2Date,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      @Size(max = 4000) String deviationNote
  ) {}
}
