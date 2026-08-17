package com.huadong.pipeline.web;


import com.huadong.pipeline.api.ProjectMilestoneApi;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ProjectMilestoneController {

  @Autowired
  private ProjectMilestoneApi api;

  @GetMapping("/studies/{studyId}/project-milestones")
  @PreAuthorize("hasAuthority('project.milestone.read')")
  ProjectMilestoneApi.ProjectMilestonePageResponse getProjectMilestones(
      @PathVariable long studyId,
      Principal principal) {
    return api.getProjectMilestones(studyId, principal.getName());
  }

  @PutMapping("/studies/{studyId}/project-milestones/{milestoneCode}")
  @PreAuthorize("hasAuthority('project.milestone.update')")
  ProjectMilestoneApi.ProjectMilestonePageResponse updateProjectMilestone(
      @PathVariable long studyId,
      @PathVariable String milestoneCode,
      @Valid @RequestBody ProjectMilestoneApi.ProjectMilestoneUpdateRequest request,
      Principal principal) {
    return api.updateProjectMilestone(studyId, milestoneCode, request, principal.getName());
  }

  @GetMapping("/studies/{studyId}/project-milestones/stage-projection")
  @PreAuthorize("hasAuthority('project.milestone.read')")
  ProjectMilestoneApi.StageProjectionResponse getProjectStageProjection(
      @PathVariable long studyId,
      Principal principal) {
    return api.getProjectStageProjection(studyId, principal.getName());
  }
}
