package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public interface PipelineConfigApi {
  List<PipelineConfigRowResponse> listRows();
  List<TherapeuticAreaResponse> listTherapeuticAreas();
  List<ProgramResponse> listPrograms(String keyword);
  List<ProjectResponse> listProjects(Long programId, String keyword);
  ProgramResponse createProgram(@Valid CreateProgramRequest request, String username);
  ProgramResponse updateProgram(long id, @Valid UpdateProgramRequest request, String username);
  RenameImpactResponse previewProgramRename(long id, @Valid RenameRequest request);
  void deleteProgram(long id, String username);
  ProjectResponse createProject(@Valid CreateProjectRequest request, String username);
  ProjectResponse updateProject(long id, @Valid UpdateProjectRequest request, String username);
  RenameImpactResponse previewProjectRename(long id, @Valid RenameRequest request);
  void deleteProject(long id, String username);
  PipelineConfigRowResponse updateStudy(long id, @Valid UpdateStudyConfigRequest request, String username);
  void deleteStudy(long id, String username);

  record CreateProgramRequest(
      @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Z0-9][A-Z0-9_-]{1,63}") String code,
      @Size(max = 200) String name,
      @NotBlank @Size(max = 200) String productName,
      @Size(max = 500) String moa,
      @NotBlank @Pattern(regexp = "SELF_DEVELOPED|IN_LICENSE|COOPERATION") String sourceCode,
      @NotBlank @Pattern(regexp = "DOMESTIC|IMPORTED") String originCode) {
  }

  record UpdateProgramRequest(
      @Size(max = 200) String name,
      @Size(max = 200) String productName,
      @Size(max = 500) String moa,
      @Pattern(regexp = "SELF_DEVELOPED|IN_LICENSE|COOPERATION") String sourceCode,
      @Pattern(regexp = "DOMESTIC|IMPORTED") String originCode,
      Boolean confirmRename,
      LocalDateTime expectedUpdatedAt,
      Long expectedProjectCount,
      Long expectedStudyCount) {
  }

  record CreateProjectRequest(
      @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Z0-9][A-Z0-9_-]{1,63}") String code,
      @Size(max = 200) String name,
      @Positive long programId,
      @NotBlank @Size(max = 500) String indication,
      @NotBlank @Size(max = 64) String therapeuticAreaCode) {
  }

  record UpdateProjectRequest(
      @Size(max = 200) String name,
      @Size(max = 500) String indication,
      @Size(max = 64) String therapeuticAreaCode,
      Boolean confirmRename,
      LocalDateTime expectedUpdatedAt,
      Long expectedStudyCount) {
  }

  record RenameRequest(@NotBlank @Size(max = 200) String newName) {
  }

  record UpdateStudyConfigRequest(
      @NotBlank @Size(max = 200) String name,
      @Positive long projectId,
      @NotBlank @Size(max = 32) String phaseStatusCode) {
  }

  record ProgramResponse(
      long id, String code, String name, String productName, String moa,
      String sourceCode, String sourceLabel, String originCode, String originLabel,
      long projectCount, long studyCount, LocalDateTime updatedAt) {
  }

  record ProjectResponse(
      long id, String code, String name, long programId, String programCode,
      String indication, long therapeuticAreaId, String therapeuticAreaCode,
      String therapeuticAreaName, long studyCount, LocalDateTime updatedAt) {
  }

  record PipelineConfigRowResponse(
      long studyId, String studyCode, String studyName,
      String phaseStatusCode, String phaseStatusLabel,
      long projectId, String projectCode, String projectName,
      String indication, String therapeuticAreaCode, String therapeuticAreaName,
      long programId, String programCode, String programName,
      String productName, String moa, String sourceCode, String sourceLabel,
      String originCode, String originLabel, LocalDateTime updatedAt) {
  }

  record RenameImpactResponse(
      long projectCount, long studyCount, LocalDateTime expectedUpdatedAt) {
  }

  record TherapeuticAreaResponse(long id, String code, String name, String englishName) {
  }
}
