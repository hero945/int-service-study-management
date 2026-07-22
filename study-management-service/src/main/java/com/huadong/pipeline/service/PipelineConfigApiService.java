package com.huadong.pipeline.service;

import com.huadong.pipeline.api.PipelineConfigApi;
import com.huadong.pipeline.domain.config.PipelineConfigRow;
import com.huadong.pipeline.domain.config.Program;
import com.huadong.pipeline.domain.config.Project;
import com.huadong.pipeline.manager.PipelineConfigManager;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PipelineConfigApiService implements PipelineConfigApi {
  private final PipelineConfigManager manager;

  public PipelineConfigApiService(PipelineConfigManager manager) {
    this.manager = manager;
  }

  @Override
  public List<PipelineConfigRowResponse> listRows() {
    return manager.listRows().stream().map(PipelineConfigApiService::toResponse).toList();
  }

  @Override
  public List<TherapeuticAreaResponse> listTherapeuticAreas() {
    return manager.listTherapeuticAreas().stream()
        .map(value -> new TherapeuticAreaResponse(
            value.id(), value.code(), value.name(), value.englishName()))
        .toList();
  }

  @Override
  public List<ProgramResponse> listPrograms(String keyword) {
    return manager.listPrograms(keyword).stream().map(PipelineConfigApiService::toResponse).toList();
  }

  @Override
  public List<ProjectResponse> listProjects(Long programId, String keyword) {
    return manager.listProjects(programId, keyword).stream().map(PipelineConfigApiService::toResponse).toList();
  }

  @Override
  public ProgramResponse createProgram(CreateProgramRequest request, String username) {
    return toResponse(manager.createProgram(new PipelineConfigManager.ProgramCommand(
        request.code(), request.productName(), request.moa(),
        request.sourceCode(), request.originCode()), username));
  }

  @Override
  public ProgramResponse updateProgram(long id, UpdateProgramRequest request, String username) {
    return toResponse(manager.updateProgram(id, new PipelineConfigManager.ProgramUpdate(
        request.productName(), request.moa(), request.sourceCode(),
        request.originCode()), username));
  }

  @Override
  public void deleteProgram(long id, String username) {
    manager.deleteProgram(id, username);
  }

  @Override
  public ProjectResponse createProject(CreateProjectRequest request, String username) {
    return toResponse(manager.createProject(new PipelineConfigManager.ProjectCommand(
        request.code(), request.programId(), request.indication(),
        request.therapeuticAreaCode()), username));
  }

  @Override
  public ProjectResponse updateProject(long id, UpdateProjectRequest request, String username) {
    return toResponse(manager.updateProject(id, new PipelineConfigManager.ProjectUpdate(
        request.indication(), request.therapeuticAreaCode()), username));
  }

  @Override
  public void deleteProject(long id, String username) {
    manager.deleteProject(id, username);
  }

  @Override
  public PipelineConfigRowResponse updateStudy(
      long id, UpdateStudyConfigRequest request, String username) {
    return toResponse(manager.updateStudy(
        id, request.projectId(), request.phaseStatusCode(), username));
  }

  @Override
  public void deleteStudy(long id, String username) {
    manager.deleteStudy(id, username);
  }

  private static ProgramResponse toResponse(Program value) {
    return new ProgramResponse(
        value.id(), value.code(), value.productName(), value.moa(),
        value.sourceCode(), sourceLabel(value.sourceCode()), value.originCode(),
        originLabel(value.originCode()), value.projectCount(), value.studyCount(), value.updatedAt());
  }

  private static ProjectResponse toResponse(Project value) {
    return new ProjectResponse(
        value.id(), value.code(), value.programId(), value.programCode(),
        value.indication(), value.therapeuticAreaId(), value.therapeuticAreaCode(),
        value.therapeuticAreaName(), value.studyCount(), value.updatedAt());
  }

  private static PipelineConfigRowResponse toResponse(PipelineConfigRow value) {
    return new PipelineConfigRowResponse(
        value.studyId(), value.studyCode(), value.phaseStatusCode(),
        value.projectId(), value.projectCode(), value.indication(), value.therapeuticAreaCode(),
        value.therapeuticAreaName(), value.programId(), value.programCode(),
        value.productName(), value.moa(), value.sourceCode(), sourceLabel(value.sourceCode()),
        value.originCode(), originLabel(value.originCode()), value.updatedAt());
  }

  private static String sourceLabel(String code) {
    return switch (code) {
      case "SELF_DEVELOPED" -> "自研";
      case "IN_LICENSE" -> "引进";
      case "COOPERATION" -> "合作";
      default -> code;
    };
  }

  private static String originLabel(String code) {
    return switch (code) {
      case "DOMESTIC" -> "国产";
      case "IMPORTED" -> "进口";
      default -> code;
    };
  }

}
