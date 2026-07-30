package com.huadong.pipeline.manager;

import lombok.extern.slf4j.Slf4j;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.config.PipelineConfigPage;
import com.huadong.pipeline.domain.config.PipelineConfigRepository;
import com.huadong.pipeline.domain.config.PipelineConfigRow;
import com.huadong.pipeline.domain.config.Program;
import com.huadong.pipeline.domain.config.ProgramRepository;
import com.huadong.pipeline.domain.config.Project;
import com.huadong.pipeline.domain.config.ProjectRepository;
import com.huadong.pipeline.domain.config.TherapeuticArea;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PipelineConfigManager {
  private static final Set<String> SOURCES =
      Set.of("SELF_DEVELOPED", "IN_LICENSE", "COOPERATION");
  private static final Set<String> ORIGINS = Set.of("DOMESTIC", "IMPORTED");
  private static final Set<String> PHASES = Set.of(
      "PRE_IND", "IND", "PHASE_1", "PHASE_2", "PRE_3", "PHASE_3_1", "PHASE_3_2");

  @Autowired
  private ProgramRepository programs;
  @Autowired
  private ProjectRepository projects;
  @Autowired
  private PipelineConfigRepository configuration;

  public List<PipelineConfigRow> listRows() {
    return configuration.findAll();
  }

  public PipelineConfigPage listRows(String keyword, int page, int pageSize) {
    return configuration.findPage(keyword, page, pageSize);
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

  public Program getProgram(long id) {
    return requireProgram(id);
  }

  public Project getProject(long id) {
    return requireProject(id);
  }

  public PipelineConfigRow getStudy(long id) {
    return requireStudy(id);
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
    int version = programs.findMaxVersionByCode(code).orElse(0) + 1;
    Program created = programs.create(code, command.productName().trim(),
        trimToNull(command.moa()), command.sourceCode(), command.originCode(), version, username);
    log.info("配置写入 operator={} entity=Program action=create id={} code={} version={}",
        username, created.id(), created.code(), created.version());
    return created;
  }

  @Transactional
  public Program updateProgram(long id, ProgramUpdate command, String username) {
    Program existing = requireProgram(id);
    String productName = valueOr(command.productName(), existing.productName());
    String source = valueOr(command.sourceCode(), existing.sourceCode());
    String origin = valueOr(command.originCode(), existing.originCode());
    String moa = command.moa() == null ? existing.moa() : trimToNull(command.moa());
    requireText(productName, "Product 不能为空");
    validateProgramEnums(source, origin);
    if (programs.existsByProductName(productName, id)) {
      throw new BusinessException("PRODUCT_NAME_EXISTS", "Product 已关联其他 Program");
    }
    programs.update(id, productName, moa, source, origin, command.expectedVersion(), username);
    log.info("配置写入 operator={} entity=Program action=update id={} version={}",
        username, id, command.expectedVersion());
    return requireProgram(id);
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
    log.info("配置删除 operator={} entity=Program id={} code={}", username, id, program.code());
  }

  @Transactional
  public Project createProject(ProjectCommand command, String username) {
    requireProgram(command.programId());
    String code = command.code().trim().toUpperCase();
    if (projects.findByCode(code).isPresent()) {
      throw new BusinessException("PROJECT_CODE_EXISTS", "Project 编码已存在");
    }
    int version = projects.findMaxVersionByCode(code).orElse(0) + 1;
    try {
      Project created = projects.create(code, command.programId(),
          command.indication().trim(), command.therapeuticAreaCode().trim().toUpperCase(),
          version, username);
      log.info("配置写入 operator={} entity=Project action=create id={} code={} version={}",
          username, created.id(), created.code(), created.version());
      return created;
    } catch (IllegalArgumentException ex) {
      throw new BusinessException("INVALID_THERAPEUTIC_AREA", "治疗领域不存在或已停用");
    }
  }

  @Transactional
  public Project updateProject(long id, ProjectUpdate command, String username) {
    requireProject(id);
    String indication = valueOr(command.indication(), existing(id).indication());
    String area = valueOr(command.therapeuticAreaCode(), existing(id).therapeuticAreaCode()).toUpperCase();
    requireText(indication, "Indication 不能为空");
    try {
      projects.update(id, indication, area, command.expectedVersion(), username);
    } catch (IllegalArgumentException ex) {
      throw new BusinessException("INVALID_THERAPEUTIC_AREA", "治疗领域不存在或已停用");
    }
    log.info("配置写入 operator={} entity=Project action=update id={} version={}",
        username, id, command.expectedVersion());
    return requireProject(id);
  }

  @Transactional
  public void deleteProject(long id, String username) {
    Project project = requireProject(id);
    if (project.studyCount() > 0) {
      throw new BusinessException("PROJECT_IN_USE", "Project 仍有关联 Study，不能删除",
          Map.of("studyCount", String.valueOf(project.studyCount())));
    }
    projects.softDelete(id, username);
    log.info("配置删除 operator={} entity=Project id={} code={}", username, id, project.code());
  }

  @Transactional
  public PipelineConfigRow updateStudy(
      long id, long projectId, String phaseStatusCode, int expectedVersion, String username) {
    requireStudy(id);
    requireProject(projectId);
    String phase = normalizePhase(phaseStatusCode);
    configuration.updateStudy(id, projectId, phase, expectedVersion, username);
    log.info(
        "配置写入 operator={} entity=Study action=update id={} projectId={} phase={} version={}",
        username, id, projectId, phase, expectedVersion);
    return requireStudy(id);
  }

  private Project existing(long id) {
    return requireProject(id);
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
    log.info("配置删除 operator={} entity=Study id={}", username, id);
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

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
  }

  public record ProgramCommand(
      String code, String productName, String moa,
      String sourceCode, String originCode) {
  }

  public record ProgramUpdate(
      String productName, String moa, String sourceCode, String originCode,
      int expectedVersion) {
  }

  public record ProjectCommand(
      String code, long programId, String indication, String therapeuticAreaCode) {
  }

  public record ProjectUpdate(
      String indication, String therapeuticAreaCode,
      int expectedVersion) {
  }
}
