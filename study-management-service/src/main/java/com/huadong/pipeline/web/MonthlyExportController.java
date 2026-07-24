package com.huadong.pipeline.web;


import com.huadong.pipeline.api.MonthlyExportApi;
import com.huadong.pipeline.common.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/reports/monthly")
public class MonthlyExportController {

  @Autowired
  private MonthlyExportApi api;

  @GetMapping("/preview")
  @PreAuthorize("hasAuthority('report.page.view')")
  MonthlyExportApi.MonthlyExportReportResponse preview(
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate,
      @RequestParam(defaultValue = "ALL") String scopeType,
      @RequestParam(required = false) List<Long> taIds,
      @RequestParam(required = false) List<Long> programIds,
      Principal principal) {
    return api.preview(query(startDate, endDate, scopeType, taIds, programIds),
        principal.getName());
  }

  @GetMapping("/export")
  @PreAuthorize("hasAuthority('report.export')")
  ResponseEntity<byte[]> export(
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate,
      @RequestParam(defaultValue = "ALL") String scopeType,
      @RequestParam(required = false) List<Long> taIds,
      @RequestParam(required = false) List<Long> programIds,
      @RequestParam String format,
      Principal principal) {
    MonthlyExportApi.MonthlyExportFileResponse file = api.export(
        query(startDate, endDate, scopeType, taIds, programIds),
        format,
        principal.getName());
    MediaType mediaType;
    try {
      mediaType = MediaType.parseMediaType(file.contentType());
    } catch (Exception ex) {
      throw new BusinessException("INVALID_EXPORT_FORMAT", "无法解析导出 Content-Type");
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(file.filename(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .contentType(mediaType)
        .body(file.body());
  }

  private static MonthlyExportApi.MonthlyExportQuery query(
      LocalDate startDate,
      LocalDate endDate,
      String scopeType,
      List<Long> taIds,
      List<Long> programIds) {
    return new MonthlyExportApi.MonthlyExportQuery(
        startDate,
        endDate,
        scopeType,
        taIds == null ? List.of() : taIds,
        programIds == null ? List.of() : programIds);
  }
}
