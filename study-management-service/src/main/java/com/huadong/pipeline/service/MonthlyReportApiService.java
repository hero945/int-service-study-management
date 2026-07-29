package com.huadong.pipeline.service;


import com.huadong.pipeline.api.MonthlyReportApi;
import com.huadong.pipeline.audit.BusinessAuditService;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.manager.MonthlyReportManager;
import com.huadong.pipeline.manager.MonthlyReportManager.EntryInput;
import com.huadong.pipeline.manager.MonthlyReportManager.FunctionLineHistoryResult;
import com.huadong.pipeline.manager.MonthlyReportManager.HistoryMonthResult;
import com.huadong.pipeline.manager.MonthlyReportManager.MonthlyEntryResult;
import com.huadong.pipeline.manager.MonthlyReportManager.MonthlyLineResult;
import com.huadong.pipeline.manager.MonthlyReportManager.MonthlyPageResult;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyReportApiService implements MonthlyReportApi {

  @Autowired
  private MonthlyReportManager manager;
  @Autowired
  private BusinessAuditService audit;

  @Override
  public MonthlyReportPageResponse getMonthlyReports(long studyId, String month, String username) {
    return page(manager.getMonthlyReports(studyId, parseMonth(month), username));
  }

  @Override
  @Transactional
  public MonthlyReportPageResponse createEntry(long reportId,
                                               MonthlyEntryCreateRequest request,
                                               String username) {
    var result = manager.createEntry(
        reportId, new EntryInput(request.entryDate(), request.content()), username);
    var after = result.after();
    audit.successGrouped(
        "MONTHLY", "MONTHLY_ENTRY", after.entryId(), String.valueOf(after.entryId()),
        after.studyId(), "MONTHLY_FUNCTION", after.reportId(), after.functionCode(),
        "monthly_save", "hd_plt_monthly_report_entry", after.entryId(),
        null, after, auditReason(after, true), username);
    return page(result.page());
  }

  @Override
  @Transactional
  public MonthlyReportPageResponse updateEntry(long entryId,
                                               MonthlyEntryUpdateRequest request,
                                               String username) {
    var result = manager.updateEntry(
        entryId, new EntryInput(request.entryDate(), request.content()), username);
    audit.successGrouped(
        "MONTHLY", "MONTHLY_ENTRY", entryId, String.valueOf(entryId),
        result.after().studyId(), "MONTHLY_FUNCTION",
        result.after().reportId(), result.after().functionCode(),
        "monthly_save", "hd_plt_monthly_report_entry", entryId,
        result.before(), result.after(), auditReason(result.after(), false), username);
    return page(result.page());
  }

  @Override
  @Transactional
  public MonthlyReportPageResponse deleteEntry(long entryId, String username) {
    var result = manager.deleteEntry(entryId, username);
    audit.successGrouped(
        "MONTHLY", "MONTHLY_ENTRY", entryId, String.valueOf(entryId),
        result.before().studyId(), "MONTHLY_FUNCTION",
        result.before().reportId(), result.before().functionCode(),
        "monthly_delete", "hd_plt_monthly_report_entry", entryId,
        result.before(), java.util.Map.of("deleted", true), "soft delete", username);
    return page(result.page());
  }

  @Override
  public FunctionLineHistoryResponse getMonthlyReportHistory(
      long studyId, long functionLineId, String month, String username) {
    FunctionLineHistoryResult result = manager.getMonthlyReportHistory(
        studyId, functionLineId, parseMonth(month), username);
    List<HistoryMonthResponse> months = result.months().stream()
        .map(m -> new HistoryMonthResponse(m.month(), m.entries().stream()
            .map(e -> new MonthlyEntryResponse(
                e.entryId(), e.entryDate(), e.content(), e.updatedBy(), e.updatedAt(), e.editable()))
            .toList()))
        .toList();
    return new FunctionLineHistoryResponse(
        result.functionLineId(), result.functionCode(), result.functionName(), months);
  }

  // ──────────── mapping helpers ────────────

  private static YearMonth parseMonth(String month) {
    try {
      return YearMonth.parse(month == null ? "" : month.trim());
    } catch (DateTimeParseException e) {
      throw new BusinessException("MONTHLY_INVALID", "月份格式必须为 YYYY-MM");
    }
  }

  private static MonthlyReportPageResponse page(MonthlyPageResult result) {
    List<FunctionLineReportResponse> lines = result.lines().stream()
        .map(MonthlyReportApiService::line)
        .toList();
    return new MonthlyReportPageResponse(
        result.studyId(), result.studyCode(), result.month(), lines);
  }

  private static String auditReason(
      com.huadong.pipeline.domain.monthly.MonthlyReportPort.EntryWithReport entry,
      boolean isNew) {
    return "studyId=" + entry.studyId()
        + " month=" + java.time.YearMonth.from(entry.reportMonth())
        + " functionCode=" + entry.functionCode()
        + " isNew=" + isNew;
  }

  private static FunctionLineReportResponse line(MonthlyLineResult line) {
    List<MonthlyEntryResponse> entries = line.entries().stream()
        .map(e -> new MonthlyEntryResponse(
            e.entryId(), e.entryDate(), e.content(), e.updatedBy(), e.updatedAt(), e.editable()))
        .toList();
    return new FunctionLineReportResponse(
        line.reportId(), line.functionLineId(), line.functionCode(), line.functionName(),
        line.editable(), entries);
  }
}
