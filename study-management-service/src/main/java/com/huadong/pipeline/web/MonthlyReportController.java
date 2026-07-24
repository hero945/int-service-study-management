package com.huadong.pipeline.web;


import com.huadong.pipeline.api.MonthlyReportApi;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class MonthlyReportController {

  @Autowired
  private MonthlyReportApi api;

  @GetMapping("/studies/{studyId}/monthly-reports")
  @PreAuthorize("hasAuthority('monthly.read')")
  MonthlyReportApi.MonthlyReportPageResponse getMonthlyReports(
      @PathVariable long studyId, @RequestParam String month, Principal principal) {
    return api.getMonthlyReports(studyId, month, principal.getName());
  }

  @PostMapping("/monthly-reports/{reportId}/entries")
  @PreAuthorize("hasAuthority('monthly.create')")
  MonthlyReportApi.MonthlyReportPageResponse createEntry(
      @PathVariable long reportId,
      @Valid @RequestBody MonthlyReportApi.MonthlyEntryCreateRequest request,
      Principal principal) {
    return api.createEntry(reportId, request, principal.getName());
  }

  @PatchMapping("/monthly-report-entries/{entryId}")
  @PreAuthorize("hasAuthority('monthly.update')")
  MonthlyReportApi.MonthlyReportPageResponse updateEntry(
      @PathVariable long entryId,
      @Valid @RequestBody MonthlyReportApi.MonthlyEntryUpdateRequest request,
      Principal principal) {
    return api.updateEntry(entryId, request, principal.getName());
  }

  @DeleteMapping("/monthly-report-entries/{entryId}")
  @PreAuthorize("hasAuthority('monthly.update')")
  MonthlyReportApi.MonthlyReportPageResponse deleteEntry(
      @PathVariable long entryId, Principal principal) {
    return api.deleteEntry(entryId, principal.getName());
  }

  @GetMapping("/studies/{studyId}/monthly-reports/history")
  @PreAuthorize("hasAuthority('monthly.read')")
  MonthlyReportApi.FunctionLineHistoryResponse getMonthlyReportHistory(
      @PathVariable long studyId,
      @RequestParam long functionLineId,
      @RequestParam String month,
      Principal principal) {
    return api.getMonthlyReportHistory(studyId, functionLineId, month, principal.getName());
  }
}
