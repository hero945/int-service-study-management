package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public interface MilestoneApi {

  /** GET /api/v1/studies/{studyId}/milestones */
  MilestonePageResponse getMilestones(long studyId, String username);

  /** PUT /api/v1/studies/{studyId}/milestones/{milestoneCode} */
  MilestonePageResponse updateMilestone(long studyId, String milestoneCode,
      @Valid MilestoneUpdateRequest request, String username);

  /** GET /api/v1/studies/{studyId}/stage-projection */
  StageProjectionResponse getStageProjection(long studyId, String username);

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

  record MilestonePageResponse(
      String studyCode,
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

  record MilestoneUpdateRequest(
      LocalDate planV1Date,
      LocalDate planV2Date,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      @Size(max = 4000) String deviationNote
  ) {}
}
