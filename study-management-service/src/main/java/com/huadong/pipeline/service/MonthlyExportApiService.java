package com.huadong.pipeline.service;


import com.huadong.pipeline.api.MonthlyExportApi;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.manager.MonthlyExportManager;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportProgress;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportQuery;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportReport;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportRisk;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportSnapshotGroup;
import com.huadong.pipeline.manager.MonthlyExportManager.ExportSnapshotRow;
import com.huadong.pipeline.service.export.MonthlyExportFileBuilder;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonthlyExportApiService implements MonthlyExportApi {

  @Autowired
  private MonthlyExportManager manager;

  @Override
  public MonthlyExportReportResponse preview(MonthlyExportQuery query, String username) {
    return toResponse(manager.build(toManagerQuery(query), username));
  }

  @Override
  public MonthlyExportFileResponse export(MonthlyExportQuery query, String format, String username) {
    ExportReport report = manager.build(toManagerQuery(query), username);
    String normalized = format == null ? "" : format.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "html" -> new MonthlyExportFileResponse(
          filename(report, "html"),
          "text/html;charset=UTF-8",
          MonthlyExportFileBuilder.html(report));
      case "csv" -> new MonthlyExportFileResponse(
          filename(report, "csv"),
          "text/csv;charset=UTF-8",
          MonthlyExportFileBuilder.csv(report));
      case "xlsx" -> new MonthlyExportFileResponse(
          filename(report, "xlsx"),
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          MonthlyExportFileBuilder.xlsx(report));
      default -> throw new BusinessException("INVALID_EXPORT_FORMAT",
          "不支持的导出格式：" + format);
    };
  }

  private static ExportQuery toManagerQuery(MonthlyExportQuery query) {
    return new ExportQuery(
        query.startDate(),
        query.endDate(),
        query.scopeType(),
        query.taIds(),
        query.programIds());
  }

  private static MonthlyExportReportResponse toResponse(ExportReport report) {
    return new MonthlyExportReportResponse(
        new MonthlyExportMetaResponse(
            report.meta().startDate(),
            report.meta().endDate(),
            report.meta().scopeType(),
            report.meta().scopeLabels(),
            report.meta().generatedAt()),
        new MonthlyExportSummaryResponse(
            report.summary().total(),
            report.summary().notStarted(),
            report.summary().inProgress(),
            report.summary().completed(),
            report.summary().reportedStudyCount(),
            report.summary().openRiskCount()),
        report.snapshotGroups().stream().map(MonthlyExportApiService::group).toList(),
        report.progress().stream().map(MonthlyExportApiService::progress).toList(),
        report.openRisks().stream().map(MonthlyExportApiService::risk).toList());
  }

  private static MonthlyExportSnapshotGroupResponse group(ExportSnapshotGroup group) {
    return new MonthlyExportSnapshotGroupResponse(
        group.taCode(),
        group.taName(),
        group.rows().stream().map(MonthlyExportApiService::row).toList());
  }

  private static MonthlyExportSnapshotRowResponse row(ExportSnapshotRow row) {
    return new MonthlyExportSnapshotRowResponse(
        row.programCode(),
        row.productName(),
        row.studyCode(),
        row.indication(),
        row.phase(),
        row.projectStatus());
  }

  private static MonthlyExportProgressResponse progress(ExportProgress item) {
    return new MonthlyExportProgressResponse(
        item.studyCode(),
        item.programCode(),
        item.taName(),
        item.entryDate(),
        item.functionCode(),
        item.functionName(),
        item.content());
  }

  private static MonthlyExportRiskResponse risk(ExportRisk risk) {
    return new MonthlyExportRiskResponse(
        risk.riskCode(),
        risk.programCode(),
        risk.description(),
        risk.score(),
        risk.level(),
        risk.ownerName());
  }

  private static String filename(ExportReport report, String extension) {
    return "研发管线月报_"
        + report.meta().startDate()
        + "_"
        + report.meta().endDate()
        + "."
        + extension;
  }
}
