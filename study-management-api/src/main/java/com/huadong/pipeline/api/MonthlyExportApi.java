package com.huadong.pipeline.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Monthly report export contract: preview read-model and file download.
 */
public interface MonthlyExportApi {

  /** GET /api/v1/reports/monthly/preview */
  MonthlyExportReportResponse preview(MonthlyExportQuery query, String username);

  /** GET /api/v1/reports/monthly/export — returns bytes + filename + content type. */
  MonthlyExportFileResponse export(MonthlyExportQuery query, String format, String username);

  record MonthlyExportQuery(
      LocalDate startDate,
      LocalDate endDate,
      String scopeType,
      List<Long> taIds,
      List<Long> programIds) {}

  record MonthlyExportReportResponse(
      MonthlyExportMetaResponse meta,
      MonthlyExportSummaryResponse summary,
      List<MonthlyExportSnapshotGroupResponse> snapshotGroups,
      List<MonthlyExportProgressResponse> progress,
      List<MonthlyExportRiskResponse> openRisks) {}

  record MonthlyExportMetaResponse(
      LocalDate startDate,
      LocalDate endDate,
      String scopeType,
      List<String> scopeLabels,
      Instant generatedAt) {}

  record MonthlyExportSummaryResponse(
      long total,
      long notStarted,
      long inProgress,
      long completed,
      long reportedStudyCount,
      long openRiskCount) {}

  record MonthlyExportSnapshotGroupResponse(
      String taCode,
      String taName,
      List<MonthlyExportSnapshotRowResponse> rows) {}

  record MonthlyExportSnapshotRowResponse(
      String programCode,
      String productName,
      String studyCode,
      String indication,
      String phase,
      String projectStatus) {}

  record MonthlyExportProgressResponse(
      String studyCode,
      String programCode,
      String taName,
      LocalDate entryDate,
      String functionCode,
      String functionName,
      String content) {}

  record MonthlyExportRiskResponse(
      String riskCode,
      String programCode,
      String description,
      int score,
      String level,
      String ownerName) {}

  record MonthlyExportFileResponse(
      String filename,
      String contentType,
      byte[] body) {}
}
