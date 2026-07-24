package com.huadong.pipeline.manager;

import lombok.extern.slf4j.Slf4j;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.config.PipelineConfigRepository;
import com.huadong.pipeline.domain.config.Program;
import com.huadong.pipeline.domain.config.ProgramRepository;
import com.huadong.pipeline.domain.config.TherapeuticArea;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition.MilestoneNode;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.ExportProgressEntry;
import com.huadong.pipeline.domain.risk.RiskRepository;
import com.huadong.pipeline.domain.risk.RiskRepository.RiskSummary;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.study.StudyRepository;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MonthlyExportManager {

  public static final String SCOPE_ALL = "ALL";
  public static final String SCOPE_TA = "TA";
  public static final String SCOPE_PROGRAM = "PROGRAM";

  public static final String STATUS_NOT_STARTED = "未开始";
  public static final String STATUS_IN_PROGRESS = "进行中";
  public static final String STATUS_COMPLETED = "已完成";

  @Autowired
  private StudyRepository studies;
  @Autowired
  private UserAccountRepository users;
  @Autowired
  private MonthlyReportPort monthlyReports;
  @Autowired
  private RiskRepository risks;
  @Autowired
  private PipelineConfigRepository configuration;
  @Autowired
  private ProgramRepository programs;
  @Autowired
  private StudyMilestonePort milestones;

  @Transactional(readOnly = true)
  public ExportReport build(ExportQuery query, String username) {
    validate(query);
    StudyAccessScope scope = accessScope(username);
    List<Study> visible = studies.findAll(scope);
    ScopeResolution resolved = resolveScope(query, visible);
    List<Study> scoped = resolved.studies();

    List<Long> studyIds = scoped.stream().map(Study::id).toList();
    Map<Long, List<PersistedMilestone>> milestonesByStudy = milestones.findByStudyIds(studyIds)
        .stream()
        .collect(Collectors.groupingBy(PersistedMilestone::studyId));

    List<ExportProgressEntry> progressEntries = monthlyReports.findProgressEntries(
        studyIds, query.startDate(), query.endDate());
    List<RiskSummary> openRisks = risks.findOpenByStudyIds(scope, studyIds);

    Set<Long> reportedStudyIds = progressEntries.stream()
        .map(ExportProgressEntry::studyId)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    Map<Long, String> statusByStudy = new LinkedHashMap<>();
    long notStarted = 0;
    long inProgress = 0;
    long completed = 0;
    for (Study study : scoped) {
      String status = deriveMilestoneStatus(
          milestonesByStudy.getOrDefault(study.id(), List.of()));
      statusByStudy.put(study.id(), status);
      switch (status) {
        case STATUS_IN_PROGRESS -> inProgress++;
        case STATUS_COMPLETED -> completed++;
        default -> notStarted++;
      }
    }

    ExportSummary summary = new ExportSummary(
        scoped.size(), notStarted, inProgress, completed,
        reportedStudyIds.size(), openRisks.size());

    Map<String, List<Study>> byTa = new LinkedHashMap<>();
    for (Study study : scoped.stream()
        .sorted(Comparator
            .comparing((Study s) -> nullToEmpty(s.therapeuticAreaName()))
            .thenComparing(s -> nullToEmpty(s.programCode()))
            .thenComparing(Study::code))
        .toList()) {
      String key = nullToEmpty(study.therapeuticAreaCode()) + "\0"
          + nullToEmpty(study.therapeuticAreaName());
      byTa.computeIfAbsent(key, ignored -> new ArrayList<>()).add(study);
    }

    List<ExportSnapshotGroup> snapshotGroups = new ArrayList<>();
    for (Map.Entry<String, List<Study>> entry : byTa.entrySet()) {
      String[] parts = entry.getKey().split("\0", 2);
      String taCode = parts[0];
      String taName = parts.length > 1 ? parts[1] : "";
      List<ExportSnapshotRow> rows = entry.getValue().stream()
          .map(study -> new ExportSnapshotRow(
              nullToEmpty(study.programCode()),
              nullToEmpty(study.productName()),
              study.code(),
              nullToEmpty(study.indication()),
              nullToEmpty(study.phase()),
              statusByStudy.getOrDefault(study.id(), STATUS_NOT_STARTED)))
          .toList();
      snapshotGroups.add(new ExportSnapshotGroup(taCode, taName, rows));
    }

    List<ExportProgress> progress = progressEntries.stream()
        .map(entry -> new ExportProgress(
            entry.studyCode(),
            entry.programCode(),
            entry.taName(),
            entry.entryDate(),
            entry.functionCode(),
            entry.functionName(),
            entry.content()))
        .toList();

    List<ExportRisk> riskRows = openRisks.stream()
        .map(risk -> new ExportRisk(
            risk.riskCode(),
            risk.programCode(),
            risk.description(),
            risk.score(),
            risk.level().name(),
            risk.ownerName()))
        .toList();

    ExportReport report = new ExportReport(
        new ExportMeta(
            query.startDate(),
            query.endDate(),
            resolved.scopeType(),
            resolved.scopeLabels(),
            Instant.now()),
        summary,
        snapshotGroups,
        progress,
        riskRows);
    log.info(
        "月报导出 operator={} scopeType={} scopeLabels={} start={} end={} studyCount={} result=success",
        username,
        resolved.scopeType(),
        resolved.scopeLabels(),
        query.startDate(),
        query.endDate(),
        summary.total());
    return report;
  }

  /**
   * Export Study status from milestones:
   * - 未开始: no milestone rows, or no actual start/end filled on any row
   * - 已完成: last child of the last stage has actual_end_date
   * - 进行中: otherwise
   */
  static String deriveMilestoneStatus(List<PersistedMilestone> rows) {
    if (rows == null || rows.isEmpty()) {
      return STATUS_NOT_STARTED;
    }
    boolean anyActual = rows.stream().anyMatch(row ->
        row.actualStartDate() != null || row.actualEndDate() != null);
    if (!anyActual) {
      return STATUS_NOT_STARTED;
    }
    List<MilestoneNode> ordered = MilestoneDefinition.orderedNodes();
    if (ordered.isEmpty()) {
      return STATUS_IN_PROGRESS;
    }
    String lastCode = ordered.get(ordered.size() - 1).code();
    boolean lastCompleted = rows.stream()
        .anyMatch(row -> lastCode.equals(row.milestoneCode()) && row.actualEndDate() != null);
    return lastCompleted ? STATUS_COMPLETED : STATUS_IN_PROGRESS;
  }

  private void validate(ExportQuery query) {
    if (query.startDate() == null || query.endDate() == null) {
      throw new BusinessException("INVALID_DATE_RANGE", "请选择汇报开始与结束日期");
    }
    if (query.endDate().isBefore(query.startDate())) {
      throw new BusinessException("INVALID_DATE_RANGE", "结束日期不能早于开始日期");
    }
    String scopeType = normalizeScope(query.scopeType());
    if (SCOPE_TA.equals(scopeType)
        && (query.taIds() == null || query.taIds().isEmpty())) {
      throw new BusinessException("INVALID_SCOPE", "请至少选择一个治疗领域");
    }
    if (SCOPE_PROGRAM.equals(scopeType)
        && (query.programIds() == null || query.programIds().isEmpty())) {
      throw new BusinessException("INVALID_SCOPE", "请至少选择一个 Program");
    }
  }

  private ScopeResolution resolveScope(ExportQuery query, List<Study> visible) {
    String scopeType = normalizeScope(query.scopeType());
    return switch (scopeType) {
      case SCOPE_TA -> {
        Map<Long, TherapeuticArea> areas = configuration.findTherapeuticAreas().stream()
            .collect(Collectors.toMap(TherapeuticArea::id, area -> area, (a, b) -> a,
                LinkedHashMap::new));
        List<TherapeuticArea> selected = new ArrayList<>();
        Set<String> codes = new LinkedHashSet<>();
        for (Long id : query.taIds()) {
          TherapeuticArea area = areas.get(id);
          if (area == null) {
            throw new BusinessException("INVALID_SCOPE", "治疗领域不存在：" + id);
          }
          selected.add(area);
          codes.add(area.code());
        }
        List<Study> filtered = visible.stream()
            .filter(study -> codes.contains(nullToEmpty(study.therapeuticAreaCode())))
            .toList();
        List<String> labels = selected.stream()
            .map(area -> area.name() == null || area.name().isBlank() ? area.code() : area.name())
            .toList();
        yield new ScopeResolution(SCOPE_TA, labels, filtered);
      }
      case SCOPE_PROGRAM -> {
        List<Program> selected = new ArrayList<>();
        Set<String> codes = new LinkedHashSet<>();
        for (Long id : query.programIds()) {
          Program program = programs.findById(id)
              .orElseThrow(() -> new BusinessException("INVALID_SCOPE", "Program 不存在：" + id));
          selected.add(program);
          codes.add(program.code());
        }
        List<Study> filtered = visible.stream()
            .filter(study -> codes.contains(nullToEmpty(study.programCode())))
            .toList();
        List<String> labels = selected.stream().map(Program::code).toList();
        yield new ScopeResolution(SCOPE_PROGRAM, labels, filtered);
      }
      default -> new ScopeResolution(SCOPE_ALL, List.of("全部项目"), visible);
    };
  }

  private static String normalizeScope(String scopeType) {
    if (scopeType == null || scopeType.isBlank()) {
      return SCOPE_ALL;
    }
    String normalized = scopeType.trim().toUpperCase(Locale.ROOT);
    if (!SCOPE_ALL.equals(normalized)
        && !SCOPE_TA.equals(normalized)
        && !SCOPE_PROGRAM.equals(normalized)) {
      throw new BusinessException("INVALID_SCOPE", "不支持的导出范围：" + scopeType);
    }
    return normalized;
  }

  private StudyAccessScope accessScope(String username) {
    var user = users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
    return user.dataScope() == DataScope.ALL
        ? StudyAccessScope.all()
        : StudyAccessScope.assignedTo(user.id());
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  public record ExportQuery(
      LocalDate startDate,
      LocalDate endDate,
      String scopeType,
      List<Long> taIds,
      List<Long> programIds) {
    public ExportQuery {
      taIds = taIds == null ? List.of() : List.copyOf(taIds);
      programIds = programIds == null ? List.of() : List.copyOf(programIds);
    }
  }

  public record ExportReport(
      ExportMeta meta,
      ExportSummary summary,
      List<ExportSnapshotGroup> snapshotGroups,
      List<ExportProgress> progress,
      List<ExportRisk> openRisks) {}

  public record ExportMeta(
      LocalDate startDate,
      LocalDate endDate,
      String scopeType,
      List<String> scopeLabels,
      Instant generatedAt) {}

  public record ExportSummary(
      long total,
      long notStarted,
      long inProgress,
      long completed,
      long reportedStudyCount,
      long openRiskCount) {}

  public record ExportSnapshotGroup(
      String taCode,
      String taName,
      List<ExportSnapshotRow> rows) {}

  public record ExportSnapshotRow(
      String programCode,
      String productName,
      String studyCode,
      String indication,
      String phase,
      String projectStatus) {}

  public record ExportProgress(
      String studyCode,
      String programCode,
      String taName,
      LocalDate entryDate,
      String functionCode,
      String functionName,
      String content) {}

  public record ExportRisk(
      String riskCode,
      String programCode,
      String description,
      int score,
      String level,
      String ownerName) {}

  private record ScopeResolution(
      String scopeType,
      List<String> scopeLabels,
      List<Study> studies) {}
}
