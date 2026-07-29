package com.huadong.pipeline.service;


import com.huadong.pipeline.api.PipelineConfigApi;
import com.huadong.pipeline.audit.BusinessAuditService;
import com.huadong.pipeline.domain.config.PipelineConfigRow;
import com.huadong.pipeline.domain.config.Program;
import com.huadong.pipeline.domain.config.Project;
import com.huadong.pipeline.manager.PipelineConfigManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineConfigApiService implements PipelineConfigApi {
  @Autowired
  private PipelineConfigManager manager;
  @Autowired
  private BusinessAuditService audit;

  @Override
  public PipelineConfigPageResponse listRows(String keyword, int page, int pageSize) {
    var result = manager.listRows(keyword, page, pageSize);
    long totalPages = result.totalItems() == 0
        ? 1
        : (result.totalItems() + result.pageSize() - 1) / result.pageSize();
    return new PipelineConfigPageResponse(
        result.data().stream().map(PipelineConfigApiService::toResponse).toList(),
        result.page(),
        result.pageSize(),
        result.totalItems(),
        totalPages);
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
  @Transactional
  public ProgramResponse createProgram(CreateProgramRequest request, String username) {
    var created = toResponse(manager.createProgram(new PipelineConfigManager.ProgramCommand(
        request.code(), request.productName(), request.moa(),
        request.sourceCode(), request.originCode()), username));
    audit.success("CONFIG", "PROGRAM", created.id(), created.code(), null,
        "PROGRAM_CREATE", "hd_plt_program", created.id(), null, created, null, username);
    return created;
  }

  @Override
  @Transactional
  public ProgramResponse updateProgram(long id, UpdateProgramRequest request, String username) {
    var before = toResponse(manager.getProgram(id));
    var after = toResponse(manager.updateProgram(id, new PipelineConfigManager.ProgramUpdate(
        request.productName(), request.moa(), request.sourceCode(),
        request.originCode()), username));
    audit.success("CONFIG", "PROGRAM", id, after.code(), null,
        "PROGRAM_UPDATE", "hd_plt_program", id, before, after, null, username);
    return after;
  }

  @Override
  @Transactional
  public void deleteProgram(long id, String username) {
    var before = toResponse(manager.getProgram(id));
    manager.deleteProgram(id, username);
    audit.success("CONFIG", "PROGRAM", id, before.code(), null,
        "PROGRAM_DELETE", "hd_plt_program", id, before,
        java.util.Map.of("deleted", true), null, username);
  }

  @Override
  @Transactional
  public ProjectResponse createProject(CreateProjectRequest request, String username) {
    var created = toResponse(manager.createProject(new PipelineConfigManager.ProjectCommand(
        request.code(), request.programId(), request.indication(),
        request.therapeuticAreaCode()), username));
    audit.success("CONFIG", "PROJECT", created.id(), created.code(), null,
        "PROJECT_CREATE", "hd_plt_project", created.id(), null, created, null, username);
    return created;
  }

  @Override
  @Transactional
  public ProjectResponse updateProject(long id, UpdateProjectRequest request, String username) {
    var before = toResponse(manager.getProject(id));
    var after = toResponse(manager.updateProject(id, new PipelineConfigManager.ProjectUpdate(
        request.indication(), request.therapeuticAreaCode()), username));
    audit.success("CONFIG", "PROJECT", id, after.code(), null,
        "PROJECT_UPDATE", "hd_plt_project", id, before, after, null, username);
    return after;
  }

  @Override
  @Transactional
  public void deleteProject(long id, String username) {
    var before = toResponse(manager.getProject(id));
    manager.deleteProject(id, username);
    audit.success("CONFIG", "PROJECT", id, before.code(), null,
        "PROJECT_DELETE", "hd_plt_project", id, before,
        java.util.Map.of("deleted", true), null, username);
  }

  @Override
  @Transactional
  public PipelineConfigRowResponse updateStudy(
      long id, UpdateStudyConfigRequest request, String username) {
    var before = toResponse(manager.getStudy(id));
    var after = toResponse(manager.updateStudy(
        id, request.projectId(), request.phaseStatusCode(), username));
    audit.success("CONFIG", "STUDY", id, after.studyCode(), id,
        "STUDY_UPDATE", "hd_plt_study", id, before, after, null, username);
    return after;
  }

  @Override
  @Transactional
  public void deleteStudy(long id, String username) {
    var before = toResponse(manager.getStudy(id));
    manager.deleteStudy(id, username);
    audit.success("CONFIG", "STUDY", id, before.studyCode(), id,
        "STUDY_DELETE", "hd_plt_study", id, before,
        java.util.Map.of("deleted", true), null, username);
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
