package com.huadong.pipeline.manager;


import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.study.OverviewArea;
import com.huadong.pipeline.domain.study.OverviewProject;
import com.huadong.pipeline.domain.study.PipelineOverview;
import com.huadong.pipeline.domain.study.PipelineOverviewRepository;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.StudyRepository;
import com.huadong.pipeline.domain.study.StudyRepository.StudyListQuery;
import com.huadong.pipeline.domain.study.StudyRepository.StudyPage;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.study.OverviewStudy;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.milestone.CurrentMilestoneStatus;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import com.huadong.pipeline.domain.config.ProjectRepository;
import com.huadong.pipeline.domain.team.TeamMatrixRepository;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyManager {
  @Autowired
  private StudyRepository studies;
  @Autowired
  private UserAccountRepository users;
  @Autowired
  private ProjectRepository projects;
  @Autowired
  private PipelineOverviewRepository overviewProjects;
  @Autowired
  private StudyMilestonePort studyMilestones;
  @Autowired
  private MilestoneManager milestoneManager;
  @Autowired
  private TeamMatrixRepository team;

  public StudyListPage list(String username, StudyListQuery rawQuery) {
    UserAccount user = currentUser(username);
    StudyAccessScope scope = accessScope(user);
    StudyListQuery query = rawQuery.normalized();
    boolean canReadMilestone = user.permissions().contains("milestone.read");
    boolean filterByMilestoneStatus = !query.milestoneStatus().isBlank();

    if (!filterByMilestoneStatus) {
      StudyPage page = studies.findPage(scope, query);
      return new StudyListPage(
          enrichViews(page.data(), canReadMilestone),
          page.totalItems(), page.page(), page.pageSize());
    }

    // Milestone node labels are derived after load; filter in memory then page.
    StudyPage candidates = studies.findPage(
        scope, query.withoutMilestoneStatus().withPaging(1, 500));
    List<StudyView> matched = enrichViews(candidates.data(), canReadMilestone).stream()
        .filter(view -> query.milestoneStatus().equals(view.currentStatus()))
        .toList();
    int from = Math.min((query.page() - 1) * query.pageSize(), matched.size());
    int to = Math.min(from + query.pageSize(), matched.size());
    return new StudyListPage(
        matched.subList(from, to), matched.size(), query.page(), query.pageSize());
  }

  private List<StudyView> enrichViews(List<Study> rows, boolean canReadMilestone) {
    if (rows.isEmpty()) {
      return List.of();
    }
    Set<Long> studyIds = rows.stream().map(Study::id).collect(Collectors.toSet());
    Map<Long, String> plNames = team.findRoleMemberNames(studyIds, "PL");
    Map<Long, String> pmNames = team.findRoleMemberNames(studyIds, "PM");
    Map<Long, List<PersistedMilestone>> milestonesByStudy = canReadMilestone
        ? studyMilestones.findByStudyIds(List.copyOf(studyIds)).stream()
            .collect(Collectors.groupingBy(PersistedMilestone::studyId))
        : Map.of();
    return rows.stream()
        .map(study -> {
          CurrentMilestoneStatus.PhaseStatus derived = canReadMilestone
              ? CurrentMilestoneStatus.derive(
                  milestonesByStudy.getOrDefault(study.id(), List.of()))
              : CurrentMilestoneStatus.PhaseStatus.EMPTY;
          String currentPhase = canReadMilestone ? derived.phase() : "";
          String currentStatus = canReadMilestone ? derived.status() : study.status().label();
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
              currentPhase,
              currentStatus,
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
    var user = currentUser(username);
    var accessScope = accessScope(user);
    var projects = overviewProjects.findOverviewProjects(accessScope);
    boolean canReadMilestone = user.permissions().contains("milestone.read");

    // Batch-load milestones for every study in the overview (single IN query, no N+1).
    List<Long> studyIds = projects.stream()
        .flatMap(project -> project.studies().stream())
        .map(OverviewStudy::id)
        .toList();
    Set<Long> studyIdSet = Set.copyOf(studyIds);
    Map<Long, List<PersistedMilestone>> milestonesByStudy = canReadMilestone
        ? studyMilestones.findByStudyIds(studyIds).stream()
            .collect(Collectors.groupingBy(PersistedMilestone::studyId))
        : Map.of();
    Map<Long, String> plNames = team.findRoleMemberNames(studyIdSet, "PL");
    Map<Long, String> pmNames = team.findRoleMemberNames(studyIdSet, "PM");

    // Override each study's status with its milestone-derived status where milestones exist.
    List<OverviewProject> enriched = projects.stream()
        .map(project -> enrichProject(project, milestonesByStudy, plNames, pmNames, canReadMilestone))
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
      Map<Long, String> pmNames,
      boolean canReadMilestone) {
    List<OverviewStudy> enrichedStudies = project.studies().stream()
        .map(study -> {
          String plName = plNames.getOrDefault(study.id(), "");
          String pmName = pmNames.getOrDefault(study.id(), "");
          if (!canReadMilestone) {
            return withOwners(study, plName, pmName);
          }
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
        nullToEmpty(study.mainStageCode()),
        nullToEmpty(study.mainStageLabel()),
        nullToEmpty(study.subStatusLabel()),
        study.preindCompleted(), study.indCompleted(), study.globallyCompleted(),
        study.currentPhaseCompleted(), plName, pmName);
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private UserAccount currentUser(String username) {
    return users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
  }

  private StudyAccessScope accessScope(UserAccount user) {
    return user.dataScope() == DataScope.ALL
        ? StudyAccessScope.all()
        : StudyAccessScope.assignedTo(user.id());
  }

  private StudyAccessScope accessScope(String username) {
    return accessScope(currentUser(username));
  }

  @Transactional
  public Study create(CreateStudyCommand command, String username) {
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
    String code = command.code().trim().toUpperCase();
    if (studies.findByCode(code).isPresent()) {
      throw new BusinessException("STUDY_CODE_EXISTS", "项目编号已存在");
    }
    int version = studies.findMaxVersionByCode(code).orElse(0) + 1;
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
      studies.save(study, version, username);
    } catch (IllegalArgumentException ex) {
      throw new BusinessException("INVALID_STUDY_HIERARCHY", "必须选择 Project 实体");
    }
    return studies.findAll(StudyAccessScope.all()).stream()
        .filter(saved -> saved.code().equals(study.code()))
        .findFirst()
        .orElseThrow(() -> new BusinessException("STUDY_NOT_FOUND", "Study 创建后未找到"));
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

  public record StudyListPage(
      List<StudyView> data, long totalItems, int page, int pageSize) {
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
