package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.study.DuplicateStudyCodeException;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.StudyRepository;
import com.huadong.pipeline.domain.study.InvalidStudyHierarchyException;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.config.ProjectRepository;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyManager {
  private final StudyRepository studies;
  private final UserAccountRepository users;
  private final ProjectRepository projects;

  public StudyManager(
      StudyRepository studies, UserAccountRepository users, ProjectRepository projects) {
    this.studies = studies;
    this.users = users;
    this.projects = projects;
  }

  public List<StudyView> list(String username) {
    return studies.findAll(accessScope(username)).stream()
        .map(study -> new StudyView(
            study.id(),
            study.code(),
            study.indication(),
            study.phase(),
            study.status(),
            study.ownerName(),
            study.startDate(),
            study.updatedAt()))
        .toList();
  }

  public PipelineOverview overview(String username) {
    var accessScope = accessScope(username);
    List<StatusMetric> metrics = Arrays.stream(StudyStatus.values())
        .map(status -> new StatusMetric(
            status,
            studies.countByStatus(status, accessScope)))
        .toList();
    return new PipelineOverview("临床研发管线", studies.count(accessScope), metrics);
  }

  private StudyAccessScope accessScope(String username) {
    var user = users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
    return user.dataScope() == DataScope.ALL
        ? StudyAccessScope.all()
        : StudyAccessScope.assignedTo(user.id());
  }

  @Transactional
  public void create(CreateStudyCommand command, String username) {
    validateDates(command);
    String programCode = command.programCode();
    String projectCode = command.projectCode();
    String therapeuticAreaCode = command.therapeuticAreaCode();
    if (command.projectId() != null) {
      var project = projects.findById(command.projectId())
          .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Project 不存在"));
      programCode = project.programCode();
      projectCode = project.code();
      therapeuticAreaCode = project.therapeuticAreaCode();
    } else if (isBlank(programCode) || isBlank(projectCode) || isBlank(therapeuticAreaCode)) {
      throw new BusinessException("INVALID_STUDY_HIERARCHY", "必须选择 Project 实体");
    }
    Study study = Study.create(
        command.code(),
        programCode,
        projectCode,
        therapeuticAreaCode,
        command.phase(),
        command.plannedStartDate(),
        command.plannedEndDate(),
        command.actualStartDate(),
        command.actualEndDate(),
        command.description());
    try {
      studies.save(study, username);
    } catch (DuplicateStudyCodeException ex) {
      throw new BusinessException("STUDY_CODE_EXISTS", "项目编号已存在");
    } catch (InvalidStudyHierarchyException ex) {
      throw new BusinessException(
          "INVALID_STUDY_HIERARCHY", "Program、Project 或治疗领域不存在，或三者关系不匹配");
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static void validateDates(CreateStudyCommand command) {
    if (command.plannedStartDate() != null && command.plannedEndDate() != null
        && command.plannedEndDate().isBefore(command.plannedStartDate())) {
      throw new BusinessException("INVALID_STUDY_DATES", "计划结束日期不能早于计划开始日期");
    }
    if (command.actualStartDate() != null && command.actualEndDate() != null
        && command.actualEndDate().isBefore(command.actualStartDate())) {
      throw new BusinessException("INVALID_STUDY_DATES", "实际结束日期不能早于实际开始日期");
    }
  }

  public record CreateStudyCommand(
      String code,
      Long projectId,
      String programCode,
      String projectCode,
      String therapeuticAreaCode,
      String phase,
      LocalDate plannedStartDate,
      LocalDate plannedEndDate,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      String description) {
  }

  public record StatusMetric(StudyStatus status, long count) {
  }

  public record PipelineOverview(String title, long total, List<StatusMetric> statuses) {
  }

  public record StudyView(
      long id,
      String code,
      String indication,
      String phase,
      StudyStatus status,
      String ownerName,
      LocalDate startDate,
      LocalDateTime updatedAt) {
  }
}
