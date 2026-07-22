package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.config.PipelineConfigRepository;
import com.huadong.pipeline.domain.config.PipelineConfigRow;
import com.huadong.pipeline.domain.config.Program;
import com.huadong.pipeline.domain.config.ProgramRepository;
import com.huadong.pipeline.domain.config.Project;
import com.huadong.pipeline.domain.config.ProjectRepository;
import com.huadong.pipeline.domain.config.RenameImpact;
import com.huadong.pipeline.domain.config.TherapeuticArea;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineConfigManager {
  private static final Set<String> SOURCES =
      Set.of("SELF_DEVELOPED", "IN_LICENSE", "COOPERATION");
  private static final Set<String> ORIGINS = Set.of("DOMESTIC", "IMPORTED");
  private static final Set<String> PHASES = Set.of(
      "PRE_IND", "IND", "PHASE_1", "PHASE_2", "PRE_3", "PHASE_3_1", "PHASE_3_2");

  private final ProgramRepository programs;
  private final ProjectRepository projects;
  private final PipelineConfigRepository configuration;

  public PipelineConfigManager(
      ProgramRepository programs,
      ProjectRepository projects,
      PipelineConfigRepository configuration) {
    this.programs = programs;
    this.projects = projects;
    this.configuration = configuration;
  }

  public List<PipelineConfigRow> listRows() {
    return configuration.findAll();
  }

  public List<TherapeuticArea> listTherapeuticAreas() {
    return configuration.findTherapeuticAreas();
  }

  public List<Program> listPrograms(String keyword) {
    return programs.findAll(keyword == null ? "" : keyword);
  }

  public List<Project> listProjects(Long programId, String keyword) {
    return projects.findAll(programId, keyword == null ? "" : keyword);
  }

  @Transactional
  public Program createProgram(ProgramCommand command, String username) {
    String code = command.code().trim().toUpperCase();
    if (programs.findByCode(code).isPresent()) {
      throw new BusinessException("PROGRAM_CODE_EXISTS", "Program 编码已存在");
    }
    if (programs.existsByProductName(command.productName().trim(), null)) {
      throw new BusinessException("PRODUCT_NAME_EXISTS", "Product 已关联其他 Program");
    }
    validateProgramEnums(command.sourceCode(), command.originCode());
    String name = trimToNull(command.name());
    return programs.create(code, name == null ? code : name, command.productName().trim(),
        trimToNull(command.moa()), command.sourceCode(), command.originCode(), username);
  }

  @Transactional
  public Program updateProgram(long id, ProgramUpdate command, String username) {
    Program existing = requireProgram(id);
    String name = valueOr(command.name(), existing.name());
    String productName = valueOr(command.productName(), existing.productName());
    String source = valueOr(command.sourceCode(), existing.sourceCode());
    String origin = valueOr(command.originCode(), existing.originCode());
    String moa = command.moa() == null ? existing.moa() : trimToNull(command.moa());
    requireText(name, "Program 名称不能为空");
    requireText(productName, "Product 不能为空");
    validateProgramEnums(source, origin);
    if (programs.existsByProductName(productName, id)) {
      throw new BusinessException("PRODUCT_NAME_EXISTS", "Product 已关联其他 Program");
    }
    LocalDateTime expected = renameConfirmation(
        !name.equals(existing.name()), command.confirmRename(), command.expectedUpdatedAt());
    if (expected != null) {
      requireImpactCount(command.expectedProjectCount(), existing.projectCount());
      requireImpactCount(command.expectedStudyCount(), existing.studyCount());
    }
    if (!programs.update(id, name, productName, moa, source, origin, expected, username)) {
      throw new BusinessException("RENAME_IMPACT_CHANGED", "影响范围已变化，请重新预览");
    }
    return requireProgram(id);
  }

  public RenameImpact previewProgramRename(long id, String newName) {
    Program program = requireProgram(id);
    requireDifferentName(program.name(), newName);
    return new RenameImpact(program.projectCount(), program.studyCount(), program.updatedAt());
  }

  @Transactional
  public void deleteProgram(long id, String username) {
    Program program = requireProgram(id);
    if (program.projectCount() > 0 || program.studyCount() > 0) {
      throw new BusinessException("PROGRAM_IN_USE", "Program 仍有关联数据，不能删除", Map.of(
          "projectCount", String.valueOf(program.projectCount()),
          "studyCount", String.valueOf(program.studyCount())));
    }
    programs.softDelete(id, username);
  }

  @Transactional
  public Project createProject(ProjectCommand command, String username) {
    requireProgram(command.programId());
    String code = command.code().trim().toUpperCase();
    if (projects.findByCode(code).isPresent()) {
      throw new BusinessException("PROJECT_CODE_EXISTS", "Project 编码已存在");
    }
    String name = trimToNull(command.name());
    try {
      return projects.create(code, name == null ? code : name, command.programId(),
          command.indication().trim(), command.therapeuticAreaCode().trim().toUpperCase(), username);
    } catch (IllegalArgumentException ex) {
      throw new BusinessException("INVALID_THERAPEUTIC_AREA", "治疗领域不存在或已停用");
    }
  }

  @Transactional
  public Project updateProject(long id, ProjectUpdate command, String username) {
    Project existing = requireProject(id);
    String name = valueOr(command.name(), existing.name());
    String indication = valueOr(command.indication(), existing.indication());
    String area = valueOr(command.therapeuticAreaCode(), existing.therapeuticAreaCode()).toUpperCase();
    requireText(name, "Project 名称不能为空");
    requireText(indication, "Indication 不能为空");
    LocalDateTime expected = renameConfirmation(
        !name.equals(existing.name()), command.confirmRename(), command.expectedUpdatedAt());
    if (expected != null) requireImpactCount(command.expectedStudyCount(), existing.studyCount());
    try {
      if (!projects.update(id, name, indication, area, expected, username)) {
        throw new BusinessException("RENAME_IMPACT_CHANGED", "影响范围已变化，请重新预览");
      }
    } catch (IllegalArgumentException ex) {
      throw new BusinessException("INVALID_THERAPEUTIC_AREA", "治疗领域不存在或已停用");
    }
    return requireProject(id);
  }

  public RenameImpact previewProjectRename(long id, String newName) {
    Project project = requireProject(id);
    requireDifferentName(project.name(), newName);
    return new RenameImpact(0, project.studyCount(), project.updatedAt());
  }

  @Transactional
  public void deleteProject(long id, String username) {
    Project project = requireProject(id);
    if (project.studyCount() > 0) {
      throw new BusinessException("PROJECT_IN_USE", "Project 仍有关联 Study，不能删除",
          Map.of("studyCount", String.valueOf(project.studyCount())));
    }
    projects.softDelete(id, username);
  }

  @Transactional
  public PipelineConfigRow updateStudy(
      long id, String name, long projectId, String phaseStatusCode, String username) {
    requireStudy(id);
    requireProject(projectId);
    String phase = normalizePhase(phaseStatusCode);
    configuration.updateStudy(id, name.trim(), projectId, phase, username);
    return requireStudy(id);
  }

  @Transactional
  public void deleteStudy(long id, String username) {
    requireStudy(id);
    var references = configuration.countStudyReferences(id);
    if (references.total() > 0) {
      throw new BusinessException("STUDY_IN_USE", "Study 仍有业务引用，不能删除", Map.of(
          "teamCount", String.valueOf(references.team()),
          "milestoneCount", String.valueOf(references.milestone()),
          "monthlyReportCount", String.valueOf(references.monthlyReport()),
          "riskCount", String.valueOf(references.risk())));
    }
    configuration.softDeleteStudy(id, username);
  }

  private Program requireProgram(long id) {
    return programs.findById(id)
        .orElseThrow(() -> new BusinessException("PROGRAM_NOT_FOUND", "Program 不存在"));
  }

  private Project requireProject(long id) {
    return projects.findById(id)
        .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Project 不存在"));
  }

  private PipelineConfigRow requireStudy(long id) {
    return configuration.findAll().stream().filter(row -> row.studyId() == id).findFirst()
        .orElseThrow(() -> new BusinessException("STUDY_NOT_FOUND", "Study 不存在"));
  }

  private static LocalDateTime renameConfirmation(
      boolean renamed, Boolean confirmed, LocalDateTime expectedUpdatedAt) {
    if (!renamed) return null;
    if (!Boolean.TRUE.equals(confirmed) || expectedUpdatedAt == null) {
      throw new BusinessException("RENAME_CONFIRMATION_REQUIRED", "请先预览并确认重命名影响范围");
    }
    return expectedUpdatedAt;
  }

  private static void requireDifferentName(String currentName, String newName) {
    if (currentName.equals(newName.trim())) {
      throw new BusinessException("NAME_UNCHANGED", "新名称与当前名称相同");
    }
  }

  private static void validateProgramEnums(String source, String origin) {
    if (!SOURCES.contains(source) || !ORIGINS.contains(origin)) {
      throw new BusinessException("INVALID_CONFIG_ENUM", "Source 或 Origin 不合法");
    }
  }

  private static String normalizePhase(String value) {
    String phase = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
    if (phase.equals("PREIND")) phase = "PRE_IND";
    if (!PHASES.contains(phase)) {
      throw new BusinessException("INVALID_CONFIG_ENUM", "Phase Status 不合法");
    }
    return phase;
  }

  private static String valueOr(String value, String fallback) {
    return value == null ? fallback : value.trim();
  }

  private static void requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new BusinessException("VALIDATION_FAILED", message);
    }
  }

  private static void requireImpactCount(Long expected, long current) {
    if (expected == null || expected != current) {
      throw new BusinessException("RENAME_IMPACT_CHANGED", "影响范围已变化，请重新预览");
    }
  }

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
  }

  public record ProgramCommand(
      String code, String name, String productName, String moa,
      String sourceCode, String originCode) {
  }

  public record ProgramUpdate(
      String name, String productName, String moa, String sourceCode, String originCode,
      Boolean confirmRename, LocalDateTime expectedUpdatedAt,
      Long expectedProjectCount, Long expectedStudyCount) {
  }

  public record ProjectCommand(
      String code, String name, long programId, String indication, String therapeuticAreaCode) {
  }

  public record ProjectUpdate(
      String name, String indication, String therapeuticAreaCode,
      Boolean confirmRename, LocalDateTime expectedUpdatedAt, Long expectedStudyCount) {
  }
}
