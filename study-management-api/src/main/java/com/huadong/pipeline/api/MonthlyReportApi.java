package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface MonthlyReportApi {

  /** GET /api/v1/studies/{studyId}/monthly-reports?month=YYYY-MM */
  MonthlyReportPageResponse getMonthlyReports(long studyId, String month, String username);

  /** POST /api/v1/monthly-reports/{reportId}/entries */
  MonthlyReportPageResponse createEntry(long reportId,
      @Valid MonthlyEntryCreateRequest request, String username);

  /** PATCH /api/v1/monthly-report-entries/{entryId} */
  MonthlyReportPageResponse updateEntry(long entryId,
      @Valid MonthlyEntryUpdateRequest request, String username);

  /** DELETE /api/v1/monthly-report-entries/{entryId} */
  MonthlyReportPageResponse deleteEntry(long entryId, String username);

  /** GET /api/v1/studies/{studyId}/monthly-reports/history?functionLineId=&month= */
  FunctionLineHistoryResponse getMonthlyReportHistory(
      long studyId, long functionLineId, String month, String username);

  // ──────────── response records ────────────

  record MonthlyReportPageResponse(
      long studyId,
      String studyCode,
      String month,
      List<FunctionLineReportResponse> functionLines
  ) {}

  record FunctionLineReportResponse(
      long reportId,
      long functionLineId,
      String functionCode,
      String functionName,
      boolean editable,
      List<MonthlyEntryResponse> entries
  ) {}

  record MonthlyEntryResponse(
      long entryId,
      LocalDate entryDate,
      String content,
      String updatedBy,
      Instant updatedAt,
      boolean editable
  ) {}

  record FunctionLineHistoryResponse(
      long functionLineId,
      String functionCode,
      String functionName,
      List<HistoryMonthResponse> months
  ) {}

  record HistoryMonthResponse(
      String month,
      List<MonthlyEntryResponse> entries
  ) {}

  // ──────────── request records ────────────

  record MonthlyEntryCreateRequest(
      @NotNull LocalDate entryDate,
      @NotBlank @Size(max = 4000) String content
  ) {}

  record MonthlyEntryUpdateRequest(
      @NotNull LocalDate entryDate,
      @NotBlank @Size(max = 4000) String content
  ) {}
}
