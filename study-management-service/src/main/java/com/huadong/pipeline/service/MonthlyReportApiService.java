package com.huadong.pipeline.service;


import com.huadong.pipeline.api.MonthlyReportApi;
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

@Service
public class MonthlyReportApiService implements MonthlyReportApi {

  @Autowired
  private MonthlyReportManager manager;

  @Override
  public MonthlyReportPageResponse getMonthlyReports(long studyId, String month, String username) {
    return page(manager.getMonthlyReports(studyId, parseMonth(month), username));
  }

  @Override
  public MonthlyReportPageResponse createEntry(long reportId,
                                               MonthlyEntryCreateRequest request,
                                               String username) {
    return page(manager.createEntry(reportId,
        new EntryInput(request.entryDate(), request.content()), username));
  }

  @Override
  public MonthlyReportPageResponse updateEntry(long entryId,
                                               MonthlyEntryUpdateRequest request,
                                               String username) {
    return page(manager.updateEntry(entryId,
        new EntryInput(request.entryDate(), request.content()), username));
  }

  @Override
  public MonthlyReportPageResponse deleteEntry(long entryId, String username) {
    return page(manager.deleteEntry(entryId, username));
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
