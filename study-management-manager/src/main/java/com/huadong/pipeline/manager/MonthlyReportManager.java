package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.EntryWithReport;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.HistoryEntry;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.FunctionLineRef;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.MaterializeReportsCommand;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.MonthlyEntryCreateCommand;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.MonthlyEntryUpdateCommand;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.PersistedMonthlyEntry;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.PersistedMonthlyReport;
import com.huadong.pipeline.domain.monthly.MonthlyReportPort.StudyRef;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyReportManager {

  private final MonthlyReportPort reports;
  private final UserAccountRepository users;

  public MonthlyReportManager(MonthlyReportPort reports, UserAccountRepository users) {
    this.reports = reports;
    this.users = users;
  }

  // ──────────── query ────────────

  @Transactional
  public MonthlyPageResult getMonthlyReports(long studyId, YearMonth month, String username) {
    UserAccount user = currentUser(username);
    StudyRef study = requireStudy(studyId);
    StudyAccessScope scope = scope(user);
    LocalDate reportMonth = month.atDay(1);

    // 可填功能线集合
    List<FunctionLineRef> fillable = fillableLines(studyId, scope);

    // 已有应填项的功能线（可见集的另一半）
    List<PersistedMonthlyReport> existing = reports.findReports(studyId, reportMonth);

    // 幂等物化：可填集 ∪ 已有应填项功能线
    Map<Long, FunctionLineRef> materializeSet = new LinkedHashMap<>();
    for (FunctionLineRef line : fillable) {
      materializeSet.put(line.id(), line);
    }
    for (PersistedMonthlyReport row : existing) {
      materializeSet.putIfAbsent(row.functionLineId(),
          new FunctionLineRef(row.functionLineId(), row.functionCode(), row.functionName()));
    }
    reports.materializeReports(new MaterializeReportsCommand(
        studyId, reportMonth, List.copyOf(materializeSet.values()), user.username()));

    return assemblePage(study, month, scope);
  }

  // ──────────── mutation ────────────

  @Transactional
  public MonthlyPageResult createEntry(long reportId, EntryInput input, String username) {
    UserAccount user = currentUser(username);
    if (!user.permissions().contains("monthly.create")) {
      throw forbidden();
    }
    PersistedMonthlyReport report = reports.findReport(reportId)
        .orElseThrow(() -> new BusinessException("MONTHLY_REPORT_NOT_FOUND",
            "月报应填项 " + reportId + " 不存在"));
    StudyAccessScope scope = scope(user);
    requireObjectAccess(scope, report.studyId(), user.id(), report.functionLineId());
    validateInput(report.reportMonth(), input);

    int sequenceNo = reports.nextSequenceNo(reportId);
    reports.saveEntry(new MonthlyEntryCreateCommand(
        reportId, input.entryDate(), sequenceNo, input.content().trim(),
        user.id(), user.username(),
        auditReason(report.studyId(), report.reportMonth(), report.functionCode(), true)));

    StudyRef study = requireStudy(report.studyId());
    return assemblePage(study, YearMonth.from(report.reportMonth()), scope);
  }

  @Transactional
  public MonthlyPageResult updateEntry(long entryId, EntryInput input, String username) {
    UserAccount user = currentUser(username);
    if (!user.permissions().contains("monthly.update")) {
      throw forbidden();
    }
    EntryWithReport entry = reports.findEntryWithReport(entryId)
        .orElseThrow(() -> new BusinessException("MONTHLY_ENTRY_NOT_FOUND",
            "月报进展明细 " + entryId + " 不存在"));
    StudyAccessScope scope = scope(user);
    requireObjectAccess(scope, entry.studyId(), user.id(), entry.functionLineId());
    validateInput(entry.reportMonth(), input);

    reports.updateEntry(new MonthlyEntryUpdateCommand(
        entryId, input.entryDate(), input.content().trim(),
        user.id(), user.username(),
        auditReason(entry.studyId(), entry.reportMonth(), entry.functionCode(), false)));

    StudyRef study = requireStudy(entry.studyId());
    return assemblePage(study, YearMonth.from(entry.reportMonth()), scope);
  }

  @Transactional
  public MonthlyPageResult deleteEntry(long entryId, String username) {
    UserAccount user = currentUser(username);
    if (!user.permissions().contains("monthly.update")) {
      throw forbidden();
    }
    EntryWithReport entry = reports.findEntryWithReport(entryId)
        .orElseThrow(() -> new BusinessException("MONTHLY_ENTRY_NOT_FOUND",
            "月报进展明细 " + entryId + " 不存在"));
    StudyAccessScope scope = scope(user);
    requireObjectAccess(scope, entry.studyId(), user.id(), entry.functionLineId());

    reports.deleteEntry(entryId, user.id(), user.username());

    StudyRef study = requireStudy(entry.studyId());
    return assemblePage(study, YearMonth.from(entry.reportMonth()), scope);
  }

  @Transactional
  public FunctionLineHistoryResult getMonthlyReportHistory(
      long studyId, long functionLineId, YearMonth month, String username) {
    UserAccount user = currentUser(username);
    if (!user.permissions().contains("monthly.read")) {
      throw forbidden();
    }
    requireStudy(studyId);
    StudyAccessScope scope = scope(user);
    if (!scope.allStudies()
        && !reports.hasAssignment(studyId, user.id(), functionLineId)) {
      throw forbidden();
    }
    FunctionLineRef line = reports.findFunctionLine(functionLineId)
        .orElseThrow(() -> new BusinessException("FUNCTION_LINE_NOT_FOUND",
            "功能线 " + functionLineId + " 不存在"));

    // 前 2 个月（不含当前月）；YearMonth.minusMonths 自动处理跨年
    YearMonth prev1 = month.minusMonths(1);
    YearMonth prev2 = month.minusMonths(2);
    List<HistoryEntry> rows = reports.findHistoryEntries(studyId, functionLineId,
        List.of(prev1.atDay(1), prev2.atDay(1)));

    Map<String, List<MonthlyEntryResult>> byMonth = new LinkedHashMap<>();
    byMonth.put(prev1.toString(), new ArrayList<>());
    byMonth.put(prev2.toString(), new ArrayList<>());
    for (HistoryEntry row : rows) {
      String key = YearMonth.from(row.reportMonth()).toString();
      byMonth.computeIfAbsent(key, k -> new ArrayList<>())
          .add(new MonthlyEntryResult(
              row.entryId(), row.entryDate(), row.content(),
              row.updatedBy(), row.updatedAt(), false));
    }
    List<HistoryMonthResult> months = byMonth.entrySet().stream()
        .map(en -> new HistoryMonthResult(en.getKey(), en.getValue()))
        .toList();
    return new FunctionLineHistoryResult(
        line.id(), line.functionCode(), line.functionName(), months);
  }

  // ──────────── assembly ────────────

  private MonthlyPageResult assemblePage(StudyRef study, YearMonth month, StudyAccessScope scope) {
    LocalDate reportMonth = month.atDay(1);
    List<PersistedMonthlyReport> rows = reports.findReports(study.id(), reportMonth);
    List<Long> reportIds = rows.stream().map(PersistedMonthlyReport::id).toList();
    Map<Long, List<PersistedMonthlyEntry>> entriesByReport = new LinkedHashMap<>();
    if (!reportIds.isEmpty()) {
      for (PersistedMonthlyEntry entry : reports.findEntries(reportIds)) {
        entriesByReport.computeIfAbsent(entry.monthlyReportId(), k -> new ArrayList<>()).add(entry);
      }
    }
    List<MonthlyLineResult> lines = new ArrayList<>();
    for (PersistedMonthlyReport row : rows) {
      boolean editable = scope.allStudies()
          || reports.hasAssignment(study.id(), scope.userId(), row.functionLineId());
      List<MonthlyEntryResult> entries = new ArrayList<>();
      for (PersistedMonthlyEntry entry
          : entriesByReport.getOrDefault(row.id(), List.of())) {
        entries.add(new MonthlyEntryResult(
            entry.id(), entry.entryDate(), entry.content(),
            entry.updatedBy(), entry.updatedAt(), editable));
      }
      lines.add(new MonthlyLineResult(
          row.id(), row.functionLineId(), row.functionCode(), row.functionName(),
          editable, entries));
    }
    return new MonthlyPageResult(study.id(), study.studyCode(), month.toString(), lines);
  }

  private List<FunctionLineRef> fillableLines(long studyId, StudyAccessScope scope) {
    if (scope.allStudies()) {
      // 管理员可填全部功能线：展示所有 ACTIVE 功能线，不限该 study 的团队分配
      return reports.findActiveFunctionLines();
    }
    return reports.findAssignedFunctionLines(studyId, scope.userId());
  }

  // ──────────── helpers ────────────

  private void requireObjectAccess(StudyAccessScope scope, long studyId,
                                   long userId, long functionLineId) {
    if (scope.allStudies()) {
      return;
    }
    if (!reports.hasAssignment(studyId, userId, functionLineId)) {
      throw forbidden();
    }
  }

  private static void validateInput(LocalDate reportMonth, EntryInput input) {
    if (input.entryDate() == null
        || !YearMonth.from(input.entryDate()).equals(YearMonth.from(reportMonth))) {
      throw invalid("进展日期必须落在报告月份 " + YearMonth.from(reportMonth) + " 内");
    }
    String content = input.content() == null ? "" : input.content().trim();
    if (content.isBlank() || content.length() > 4000) {
      throw invalid("进展内容不能为空且不能超过4000字");
    }
  }

  private static String auditReason(long studyId, LocalDate reportMonth,
                                    String functionCode, boolean isNew) {
    return "studyId=" + studyId + " month=" + YearMonth.from(reportMonth)
        + " functionCode=" + functionCode + " isNew=" + isNew;
  }

  private StudyRef requireStudy(long studyId) {
    return reports.findStudy(studyId)
        .orElseThrow(() -> new BusinessException("STUDY_NOT_FOUND",
            "Study " + studyId + " 不存在"));
  }

  private UserAccount currentUser(String username) {
    return users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
  }

  private static StudyAccessScope scope(UserAccount user) {
    return user.dataScope() == DataScope.ALL ? StudyAccessScope.all()
        : StudyAccessScope.assignedTo(user.id());
  }

  private static BusinessException invalid(String message) {
    return new BusinessException("MONTHLY_INVALID", message);
  }

  private static BusinessException forbidden() {
    return new BusinessException("MONTHLY_FORBIDDEN", "没有该功能线的月报填写权限");
  }

  // ──────────── result types ────────────

  public record EntryInput(LocalDate entryDate, String content) {}

  public record MonthlyPageResult(
      long studyId, String studyCode, String month, List<MonthlyLineResult> lines) {}

  public record MonthlyLineResult(
      long reportId, long functionLineId, String functionCode, String functionName,
      boolean editable, List<MonthlyEntryResult> entries) {}

  public record MonthlyEntryResult(
      long entryId, LocalDate entryDate, String content,
      String updatedBy, Instant updatedAt, boolean editable) {}

  public record FunctionLineHistoryResult(
      long functionLineId, String functionCode, String functionName,
      List<HistoryMonthResult> months) {}

  public record HistoryMonthResult(String month, List<MonthlyEntryResult> entries) {}
}
