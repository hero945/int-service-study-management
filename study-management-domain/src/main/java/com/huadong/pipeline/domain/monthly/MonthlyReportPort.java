package com.huadong.pipeline.domain.monthly;

import com.huadong.pipeline.domain.study.StudyAccessScope;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for {@code hd_plt_monthly_report} (应填项) and
 * {@code hd_plt_monthly_report_entry} (多次独立进展明细).
 */
public interface MonthlyReportPort {

  // ──────────── query ────────────

  /** Look up study metadata used for materialization snapshots. */
  Optional<StudyRef> findStudy(long studyId);

  /** Look up study metadata within the caller's Study data scope. */
  Optional<StudyRef> findStudy(StudyAccessScope scope, long studyId);

  /** Function lines assigned to a specific user on a study (regular-user fillable set). */
  List<FunctionLineRef> findAssignedFunctionLines(long studyId, long userId);

  /** DISTINCT function lines appearing in a study's team assignments. */
  List<FunctionLineRef> findStudyFunctionLines(long studyId);

  /** All ACTIVE function lines (admin fallback when a study has no assignments). */
  List<FunctionLineRef> findActiveFunctionLines();

  /** Persisted 应填项 rows for a study and month (reportMonth = first day of month). */
  List<PersistedMonthlyReport> findReports(long studyId, LocalDate reportMonth);

  /** Progress entries for the given report ids, ordered by report + sequence. */
  List<PersistedMonthlyEntry> findEntries(List<Long> reportIds);

  /** Load one 应填项 row by id. */
  Optional<PersistedMonthlyReport> findReport(long reportId);

  /** Load one entry joined with its parent report (for validation and object-level checks). */
  Optional<EntryWithReport> findEntryWithReport(long entryId);

  /** Whether the user has a team assignment on (studyId, functionLineId). */
  boolean hasAssignment(long studyId, long userId, long functionLineId);

  /** Look up a function line's code/name by id (for history header). */
  Optional<FunctionLineRef> findFunctionLine(long functionLineId);

  // ──────────── mutation ────────────

  /** Idempotently materialize missing 应填项 rows (INSERT … SELECT … WHERE NOT EXISTS). */
  void materializeReports(MaterializeReportsCommand command);

  /** Next sequence number within a report (COALESCE(MAX(sequence_no),0)+1). */
  int nextSequenceNo(long reportId);

  /** Insert one independent progress entry; returns the new entry id. Writes audit. */
  long saveEntry(MonthlyEntryCreateCommand command);

  /** Update an entry's content/date. Writes audit. */
  void updateEntry(MonthlyEntryUpdateCommand command);

  /** Soft-delete one entry (sys_deleted = 1). */
  void deleteEntry(long entryId, long operatorUserId, String operatorEmail);

  /** Previous-month history entries for a function line (report_month IN given months). */
  List<HistoryEntry> findHistoryEntries(long studyId, long functionLineId, List<LocalDate> reportMonths);

  /**
   * Non-empty progress entries for export: studyIds ∩ entryDate ∈ [start, end],
   * ordered by entry date then study then function line.
   */
  List<ExportProgressEntry> findProgressEntries(
      List<Long> studyIds, LocalDate startDate, LocalDate endDate);

  // ──────────── records ────────────

  record ExportProgressEntry(
      long studyId,
      String studyCode,
      String programCode,
      String taName,
      LocalDate entryDate,
      String functionCode,
      String functionName,
      String content) {}


  record StudyRef(long id, String studyCode,
                  long programId, String programCode, String productName,
                  long projectId, String projectCode,
                  long therapeuticAreaId, String therapeuticAreaCode,
                  String therapeuticAreaName, String indicationDescription) {}

  record FunctionLineRef(long id, String functionCode, String functionName) {}

  record PersistedMonthlyReport(
      long id, long studyId, LocalDate reportMonth,
      long functionLineId, String functionCode, String functionName) {}

  record PersistedMonthlyEntry(
      long id, long monthlyReportId, LocalDate entryDate, int sequenceNo,
      String content, String updatedBy, Instant updatedAt) {}

  record EntryWithReport(
      long entryId, LocalDate entryDate, int sequenceNo, String content,
      long reportId, long studyId, LocalDate reportMonth,
      long functionLineId, String functionCode, String functionName) {}

  record HistoryEntry(
      long entryId, LocalDate reportMonth, LocalDate entryDate, String content,
      String updatedBy, Instant updatedAt) {}

  record MaterializeReportsCommand(
      long studyId, LocalDate reportMonth,
      List<FunctionLineRef> functionLines, String operatorEmail) {}

  record MonthlyEntryCreateCommand(
      long reportId, LocalDate entryDate, int sequenceNo, String content,
      long operatorUserId, String operatorEmail, String auditReason) {}

  record MonthlyEntryUpdateCommand(
      long entryId, LocalDate entryDate, String content,
      long operatorUserId, String operatorEmail, String auditReason) {}
}
