package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.study.DuplicateStudyCodeException;
import com.huadong.pipeline.domain.study.OverviewArea;
import com.huadong.pipeline.domain.study.OverviewProject;
import com.huadong.pipeline.domain.study.PipelineOverview;
import com.huadong.pipeline.domain.study.PipelineOverviewRepository;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.StudyRepository;
import com.huadong.pipeline.domain.study.InvalidStudyHierarchyException;
import com.huadong.pipeline.domain.study.OverviewStudy;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.milestone.CurrentMilestoneStatus;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import com.huadong.pipeline.domain.config.ProjectRepository;
import com.huadong.pipeline.domain.team.TeamMatrixRepository;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyManager {
  private final StudyRepository studies;
  private final UserAccountRepository users;
  private final ProjectRepository projects;
  private final PipelineOverviewRepository overviewProjects;
  private final StudyMilestonePort studyMilestones;
  private final MilestoneManager milestoneManager;
  private final TeamMatrixRepository team;

  public StudyManager(
      StudyRepository studies,
      UserAccountRepository users,
      ProjectRepository projects,
      PipelineOverviewRepository overviewProjects,
      StudyMilestonePort studyMilestones,
      MilestoneManager milestoneManager,
      TeamMatrixRepository team) {
    this.studies = studies;
    this.users = users;
    this.projects = projects;
    this.overviewProjects = overviewProjects;
    this.studyMilestones = studyMilestones;
    this.milestoneManager = milestoneManager;
    this.team = team;
  }

  public List<StudyView> list(String username) {
    List<Study> all = studies.findAll(accessScope(username));
    Set<Long> studyIds = all.stream().map(Study::id).collect(Collectors.toSet());
    Map<Long, String> plNames = team.findRoleMemberNames(studyIds, "PL");
    Map<Long, String> pmNames = team.findRoleMemberNames(studyIds, "PM");
    return all.stream()
        .map(study -> {
          CurrentMilestoneStatus.PhaseStatus derived =
              CurrentMilestoneStatus.derive(studyMilestones.findByStudyId(study.id()));
          return new StudyView(
              study.id(),
              study.code(),
              study.indication(),
              study.phase(),
              study.status(),
              study.ownerName(),
              study.startDate(),
              plNames.getOrDefault(study.id(), ""),
              pmNames.getOrDefault(study.id(), ""),
              derived.phase(),
              derived.status(),
              study.updatedAt(),
              study.therapeuticAreaCode(),
              study.therapeuticAreaName(),
              study.programCode(),
              study.projectCode(),
              study.productName(),
              study.moa(),
              study.sourceCode(),
              study.originCode());
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public PipelineOverview overview(String username) {
    var accessScope = accessScope(username);
    var projects = overviewProjects.findOverviewProjects(accessScope);

    // Batch-load milestones for every study in the overview (single IN query, no N+1).
    List<Long> studyIds = projects.stream()
        .flatMap(project -> project.studies().stream())
        .map(OverviewStudy::id)
        .toList();
    Set<Long> studyIdSet = Set.copyOf(studyIds);
    Map<Long, List<PersistedMilestone>> milestonesByStudy = studyMilestones.findByStudyIds(studyIds)
        .stream()
        .collect(Collectors.groupingBy(PersistedMilestone::studyId));
    Map<Long, String> plNames = team.findRoleMemberNames(studyIdSet, "PL");
    Map<Long, String> pmNames = team.findRoleMemberNames(studyIdSet, "PM");

    // Override each study's status with its milestone-derived status where milestones exist.
    List<OverviewProject> enriched = projects.stream()
        .map(project -> enrichProject(project, milestonesByStudy, plNames, pmNames))
        .toList();

    Map<String, List<OverviewProject>> projectsByArea = new LinkedHashMap<>();
    Map<String, String> areaNames = new LinkedHashMap<>();
    for (var project : enriched) {
      projectsByArea.computeIfAbsent(project.therapeuticAreaCode(), key -> new ArrayList<>())
          .add(project);
      areaNames.putIfAbsent(project.therapeuticAreaCode(), project.therapeuticAreaName());
    }
    var areas = projectsByArea.entrySet().stream()
        .map(entry -> new OverviewArea(entry.getKey(), areaNames.get(entry.getKey()), entry.getValue()))
        .toList();
    return new PipelineOverview("临床研发管线", areas);
  }

  private OverviewProject enrichProject(
      OverviewProject project,
      Map<Long, List<PersistedMilestone>> milestonesByStudy,
      Map<Long, String> plNames,
      Map<Long, String> pmNames) {
    List<OverviewStudy> enrichedStudies = project.studies().stream()
        .map(study -> {
          String plName = plNames.getOrDefault(study.id(), "");
          String pmName = pmNames.getOrDefault(study.id(), "");
          List<PersistedMilestone> milestones = milestonesByStudy.get(study.id());
          if (milestones == null || milestones.isEmpty()) {
            return withOwners(study, plName, pmName);
          }
          MilestoneManager.MilestoneOverviewStatus derived =
              milestoneManager.computeOverviewStatus(milestones);
          if (derived == null) {
            return withOwners(study, plName, pmName);
          }
          return new OverviewStudy(
              study.id(), study.code(), study.phase(), derived.status(),
              study.startDate(), study.updatedAt(),
              derived.mainStageCode(), derived.mainStageLabel(),
              derived.subStatusLabel(),
              derived.preindCompleted(), derived.indCompleted(), derived.globallyCompleted(),
              derived.currentPhaseCompleted(), plName, pmName);
        })
        .toList();
    return new OverviewProject(
        project.id(), project.code(), project.indication(), project.programCode(),
        project.productName(), project.moa(), project.sourceCode(), project.originCode(),
        project.therapeuticAreaCode(), project.therapeuticAreaName(), enrichedStudies);
  }

  private static OverviewStudy withOwners(OverviewStudy study, String plName, String pmName) {
    return new OverviewStudy(
        study.id(), study.code(), study.phase(), study.status(),
        study.startDate(), study.updatedAt(),
        study.mainStageCode(), study.mainStageLabel(), study.subStatusLabel(),
        study.preindCompleted(), study.indCompleted(), study.globallyCompleted(),
        study.currentPhaseCompleted(), plName, pmName);
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

  public record StudyView(
      long id,
      String code,
      String indication,
      String phase,
      StudyStatus status,
      String ownerName,
      LocalDate startDate,
      String plName,
      String pmName,
      String currentPhase,
      String currentStatus,
      LocalDateTime updatedAt,
      String therapeuticAreaCode,
      String therapeuticAreaName,
      String programCode,
      String projectCode,
      String productName,
      String moa,
      String sourceCode,
      String originCode) {
  }
}
