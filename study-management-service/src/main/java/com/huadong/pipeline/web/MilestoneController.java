package com.huadong.pipeline.web;

import com.huadong.pipeline.api.MilestoneApi;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class MilestoneController {

  private final MilestoneApi api;

  public MilestoneController(MilestoneApi api) {
    this.api = api;
  }

  @GetMapping("/studies/{studyId}/milestones")
  @PreAuthorize("hasAuthority('study.read')")
  MilestoneApi.MilestonePageResponse getMilestones(@PathVariable long studyId,
                                                   Principal principal) {
    return api.getMilestones(studyId, principal.getName());
  }

  @PutMapping("/studies/{studyId}/milestones/{milestoneCode}")
  @PreAuthorize("hasAuthority('milestone.update')")
  MilestoneApi.MilestonePageResponse updateMilestone(@PathVariable long studyId,
      @PathVariable String milestoneCode,
      @Valid @RequestBody MilestoneApi.MilestoneUpdateRequest request,
      Principal principal) {
    return api.updateMilestone(studyId, milestoneCode, request, principal.getName());
  }

  @GetMapping("/studies/{studyId}/stage-projection")
  @PreAuthorize("hasAuthority('study.read')")
  MilestoneApi.StageProjectionResponse getStageProjection(@PathVariable long studyId,
                                                          Principal principal) {
    return api.getStageProjection(studyId, principal.getName());
  }
}
