package com.huadong.pipeline.web;

import com.huadong.pipeline.api.PipelineConfigApi;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinical-pipeline")
public class PipelineConfigController {
  private final PipelineConfigApi api;

  public PipelineConfigController(PipelineConfigApi api) {
    this.api = api;
  }

  @GetMapping("/pipeline-config")
  @PreAuthorize("hasAuthority('config.page.view')")
  List<PipelineConfigApi.PipelineConfigRowResponse> listRows() {
    return api.listRows();
  }

  @GetMapping("/therapeutic-areas")
  @PreAuthorize("hasAuthority('config.page.view')")
  List<PipelineConfigApi.TherapeuticAreaResponse> listTherapeuticAreas() {
    return api.listTherapeuticAreas();
  }

  @GetMapping("/programs")
  @PreAuthorize("hasAuthority('config.page.view')")
  List<PipelineConfigApi.ProgramResponse> listPrograms(
      @RequestParam(required = false) String keyword) {
    return api.listPrograms(keyword);
  }

  @PostMapping("/programs")
  @PreAuthorize("hasAuthority('config.create')")
  @ResponseStatus(HttpStatus.CREATED)
  PipelineConfigApi.ProgramResponse createProgram(
      @Valid @RequestBody PipelineConfigApi.CreateProgramRequest request, Principal principal) {
    return api.createProgram(request, principal.getName());
  }

  @PatchMapping("/programs/{id}")
  @PreAuthorize("hasAuthority('config.update')")
  PipelineConfigApi.ProgramResponse updateProgram(
      @PathVariable long id,
      @Valid @RequestBody PipelineConfigApi.UpdateProgramRequest request,
      Principal principal) {
    return api.updateProgram(id, request, principal.getName());
  }

  @PostMapping("/programs/{id}/rename-impact")
  @PreAuthorize("hasAuthority('config.update')")
  PipelineConfigApi.RenameImpactResponse previewProgramRename(
      @PathVariable long id, @Valid @RequestBody PipelineConfigApi.RenameRequest request) {
    return api.previewProgramRename(id, request);
  }

  @DeleteMapping("/programs/{id}")
  @PreAuthorize("hasAuthority('config.delete')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteProgram(@PathVariable long id, Principal principal) {
    api.deleteProgram(id, principal.getName());
  }

  @GetMapping("/projects")
  @PreAuthorize("hasAuthority('config.page.view')")
  List<PipelineConfigApi.ProjectResponse> listProjects(
      @RequestParam(required = false) Long programId,
      @RequestParam(required = false) String keyword) {
    return api.listProjects(programId, keyword);
  }

  @PostMapping("/projects")
  @PreAuthorize("hasAuthority('config.create')")
  @ResponseStatus(HttpStatus.CREATED)
  PipelineConfigApi.ProjectResponse createProject(
      @Valid @RequestBody PipelineConfigApi.CreateProjectRequest request, Principal principal) {
    return api.createProject(request, principal.getName());
  }

  @PatchMapping("/projects/{id}")
  @PreAuthorize("hasAuthority('config.update')")
  PipelineConfigApi.ProjectResponse updateProject(
      @PathVariable long id,
      @Valid @RequestBody PipelineConfigApi.UpdateProjectRequest request,
      Principal principal) {
    return api.updateProject(id, request, principal.getName());
  }

  @PostMapping("/projects/{id}/rename-impact")
  @PreAuthorize("hasAuthority('config.update')")
  PipelineConfigApi.RenameImpactResponse previewProjectRename(
      @PathVariable long id, @Valid @RequestBody PipelineConfigApi.RenameRequest request) {
    return api.previewProjectRename(id, request);
  }

  @DeleteMapping("/projects/{id}")
  @PreAuthorize("hasAuthority('config.delete')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteProject(@PathVariable long id, Principal principal) {
    api.deleteProject(id, principal.getName());
  }

  @PatchMapping("/studies/{id}")
  @PreAuthorize("hasAuthority('config.update')")
  PipelineConfigApi.PipelineConfigRowResponse updateStudy(
      @PathVariable long id,
      @Valid @RequestBody PipelineConfigApi.UpdateStudyConfigRequest request,
      Principal principal) {
    return api.updateStudy(id, request, principal.getName());
  }

  @DeleteMapping("/studies/{id}")
  @PreAuthorize("hasAuthority('config.delete')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteStudy(@PathVariable long id, Principal principal) {
    api.deleteStudy(id, principal.getName());
  }
}
