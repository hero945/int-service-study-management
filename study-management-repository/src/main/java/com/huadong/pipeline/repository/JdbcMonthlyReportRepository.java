package com.huadong.pipeline.repository;

import com.huadong.pipeline.domain.monthly.MonthlyReportPort;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMonthlyReportRepository implements MonthlyReportPort {

  private final JdbcTemplate jdbc;

  public JdbcMonthlyReportRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  // ──────────── query ────────────

  @Override
  public Optional<StudyRef> findStudy(long studyId) {
    return jdbc.query("""
        SELECT s.id, s.study_code, s.program_id, s.program_code_snapshot,
          s.product_name_snapshot, s.project_id, s.project_code_snapshot,
          s.therapeutic_area_id, s.therapeutic_area_code_snapshot,
          s.therapeutic_area_name_snapshot, s.indication_description_snapshot
        FROM hd_plt_study s WHERE s.id = ? AND s.sys_deleted = 0
        """, (rs, row) -> new StudyRef(
            rs.getLong("id"), rs.getString("study_code"),
            rs.getLong("program_id"), rs.getString("program_code_snapshot"),
            rs.getString("product_name_snapshot"),
            rs.getLong("project_id"), rs.getString("project_code_snapshot"),
            rs.getLong("therapeutic_area_id"), rs.getString("therapeutic_area_code_snapshot"),
            rs.getString("therapeutic_area_name_snapshot"),
            rs.getString("indication_description_snapshot")),
        studyId).stream().findFirst();
  }

  @Override
  public List<FunctionLineRef> findAssignedFunctionLines(long studyId, long userId) {
    return jdbc.query("""
        SELECT DISTINCT fl.id, fl.function_code, fl.function_name, fl.sort_order
        FROM hd_plt_team_assignment ta
        JOIN hd_plt_function_line fl ON fl.id = ta.function_line_id
        WHERE ta.study_id = ? AND ta.user_id = ? AND ta.sys_deleted = 0
          AND fl.status_code = 'ACTIVE' AND fl.sys_deleted = 0
        ORDER BY fl.sort_order
        """, FUNCTION_LINE_MAPPER, studyId, userId);
  }

  @Override
  public List<FunctionLineRef> findStudyFunctionLines(long studyId) {
    return jdbc.query("""
        SELECT DISTINCT fl.id, fl.function_code, fl.function_name, fl.sort_order
        FROM hd_plt_team_assignment ta
        JOIN hd_plt_function_line fl ON fl.id = ta.function_line_id
        WHERE ta.study_id = ? AND ta.sys_deleted = 0
          AND fl.status_code = 'ACTIVE' AND fl.sys_deleted = 0
        ORDER BY fl.sort_order
        """, FUNCTION_LINE_MAPPER, studyId);
  }

  @Override
  public List<FunctionLineRef> findActiveFunctionLines() {
    return jdbc.query("""
        SELECT id, function_code, function_name FROM hd_plt_function_line
        WHERE status_code = 'ACTIVE' AND sys_deleted = 0
        ORDER BY sort_order
        """, FUNCTION_LINE_MAPPER);
  }

  @Override
  public List<PersistedMonthlyReport> findReports(long studyId, LocalDate reportMonth) {
    return jdbc.query("""
        SELECT id, study_id, report_month, function_line_id,
          function_line_code_snapshot, function_line_name_snapshot
        FROM hd_plt_monthly_report
        WHERE study_id = ? AND report_month = ? AND sys_deleted = 0
        ORDER BY id
        """, (rs, row) -> report(rs), studyId, Date.valueOf(reportMonth));
  }

  @Override
  public List<PersistedMonthlyEntry> findEntries(List<Long> reportIds) {
    if (reportIds.isEmpty()) {
      return List.of();
    }
    StringJoiner placeholders = new StringJoiner(", ");
    for (int i = 0; i < reportIds.size(); i++) {
      placeholders.add("?");
    }
    return jdbc.query("""
        SELECT id, monthly_report_id, entry_date, sequence_no, progress_content,
          sys_update_by, sys_update_time
        FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id IN (""" + placeholders + """
        ) AND sys_deleted = 0
        ORDER BY monthly_report_id, sequence_no
        """, (rs, row) -> entry(rs), reportIds.toArray());
  }

  @Override
  public Optional<PersistedMonthlyReport> findReport(long reportId) {
    return jdbc.query("""
        SELECT id, study_id, report_month, function_line_id,
          function_line_code_snapshot, function_line_name_snapshot
        FROM hd_plt_monthly_report
        WHERE id = ? AND sys_deleted = 0
        """, (rs, row) -> report(rs), reportId).stream().findFirst();
  }

  @Override
  public Optional<EntryWithReport> findEntryWithReport(long entryId) {
    return jdbc.query("""
        SELECT e.id, e.entry_date, e.sequence_no, e.progress_content,
          r.id AS report_id, r.study_id, r.report_month, r.function_line_id,
          r.function_line_code_snapshot, r.function_line_name_snapshot
        FROM hd_plt_monthly_report_entry e
        JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
        WHERE e.id = ? AND e.sys_deleted = 0 AND r.sys_deleted = 0
        """, (rs, row) -> new EntryWithReport(
            rs.getLong("id"), rs.getDate("entry_date").toLocalDate(),
            rs.getInt("sequence_no"), rs.getString("progress_content"),
            rs.getLong("report_id"), rs.getLong("study_id"),
            rs.getDate("report_month").toLocalDate(),
            rs.getLong("function_line_id"), rs.getString("function_line_code_snapshot"),
            rs.getString("function_line_name_snapshot")),
        entryId).stream().findFirst();
  }

  @Override
  public boolean hasAssignment(long studyId, long userId, long functionLineId) {
    Integer count = jdbc.queryForObject("""
        SELECT COUNT(*) FROM hd_plt_team_assignment
        WHERE study_id = ? AND user_id = ? AND function_line_id = ? AND sys_deleted = 0
        """, Integer.class, studyId, userId, functionLineId);
    return count != null && count > 0;
  }

  @Override
  public Optional<FunctionLineRef> findFunctionLine(long functionLineId) {
    return jdbc.query("""
        SELECT id, function_code, function_name FROM hd_plt_function_line
        WHERE id = ? AND status_code = 'ACTIVE' AND sys_deleted = 0
        """, FUNCTION_LINE_MAPPER, functionLineId).stream().findFirst();
  }

  // ──────────── mutation ────────────

  @Override
  public void materializeReports(MaterializeReportsCommand command) {
    // Idempotent backfill of 应填项 rows; the UNIQUE(study_id, report_month,
    // function_line_id) key is the backstop against concurrent materialization.
    for (FunctionLineRef line : command.functionLines()) {
      jdbc.update("""
          INSERT INTO hd_plt_monthly_report(
            study_id, report_month, function_line_id,
            function_line_code_snapshot, function_line_name_snapshot,
            study_code_snapshot, program_code_snapshot, product_name_snapshot,
            project_code_snapshot, therapeutic_area_code_snapshot,
            therapeutic_area_name_snapshot, indication_description_snapshot,
            sys_create_by, sys_update_by)
          SELECT s.id, ?, fl.id, fl.function_code, fl.function_name,
            s.study_code, s.program_code_snapshot, s.product_name_snapshot,
            s.project_code_snapshot, s.therapeutic_area_code_snapshot,
            s.therapeutic_area_name_snapshot, s.indication_description_snapshot,
            ?, ?
          FROM hd_plt_study s
          JOIN hd_plt_function_line fl ON fl.id = ?
          WHERE s.id = ? AND s.sys_deleted = 0
            AND NOT EXISTS (
              SELECT 1 FROM hd_plt_monthly_report mr
              WHERE mr.study_id = s.id AND mr.report_month = ?
                AND mr.function_line_id = fl.id)
          """,
          Date.valueOf(command.reportMonth()),
          command.operatorEmail(), command.operatorEmail(),
          line.id(), command.studyId(), Date.valueOf(command.reportMonth()));
    }
  }

  @Override
  public int nextSequenceNo(long reportId) {
    Integer next = jdbc.queryForObject("""
        SELECT COALESCE(MAX(sequence_no), 0) + 1 FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sys_deleted = 0
        """, Integer.class, reportId);
    return next == null ? 1 : next;
  }

  @Override
  public long saveEntry(MonthlyEntryCreateCommand command) {
    jdbc.update("""
        INSERT INTO hd_plt_monthly_report_entry(
          monthly_report_id, entry_date, sequence_no, progress_content,
          sys_create_by, sys_update_by)
        VALUES (?, ?, ?, ?, ?, ?)
        """,
        command.reportId(), Date.valueOf(command.entryDate()), command.sequenceNo(),
        command.content(), command.operatorEmail(), command.operatorEmail());
    // Read the id back via the unique (monthly_report_id, sequence_no) pair
    // instead of GeneratedKeyHolder.
    Long id = jdbc.queryForObject("""
        SELECT id FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sequence_no = ? AND sys_deleted = 0
        """, Long.class, command.reportId(), command.sequenceNo());
    audit(command.operatorUserId(), command.operatorEmail(), id, command.auditReason(), "monthly_save");
    return id;
  }

  @Override
  public void updateEntry(MonthlyEntryUpdateCommand command) {
    jdbc.update("""
        UPDATE hd_plt_monthly_report_entry
        SET entry_date = ?, progress_content = ?, sys_update_by = ?,
          sys_update_time = CURRENT_TIMESTAMP
        WHERE id = ? AND sys_deleted = 0
        """,
        Date.valueOf(command.entryDate()), command.content(),
        command.operatorEmail(), command.entryId());
    audit(command.operatorUserId(), command.operatorEmail(),
        command.entryId(), command.auditReason(), "monthly_save");
  }

  @Override
  public void deleteEntry(long entryId, long operatorUserId, String operatorEmail) {
    jdbc.update("""
        UPDATE hd_plt_monthly_report_entry
        SET sys_deleted = 1, sys_update_by = ?, sys_update_time = CURRENT_TIMESTAMP
        WHERE id = ? AND sys_deleted = 0
        """, operatorEmail, entryId);
    audit(operatorUserId, operatorEmail, entryId, "soft delete", "monthly_delete");
  }

  @Override
  public List<HistoryEntry> findHistoryEntries(long studyId, long functionLineId,
                                               List<LocalDate> reportMonths) {
    if (reportMonths.isEmpty()) {
      return List.of();
    }
    StringJoiner placeholders = new StringJoiner(", ");
    for (int i = 0; i < reportMonths.size(); i++) {
      placeholders.add("?");
    }
    List<Object> args = new ArrayList<>();
    args.add(studyId);
    args.add(functionLineId);
    for (LocalDate month : reportMonths) {
      args.add(Date.valueOf(month));
    }
    String sql = "SELECT e.id, r.report_month, e.entry_date, e.progress_content, "
        + "e.sys_update_by, e.sys_update_time "
        + "FROM hd_plt_monthly_report_entry e "
        + "JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id "
        + "WHERE r.study_id = ? AND r.function_line_id = ? AND r.report_month IN ("
        + placeholders + ") "
        + "AND e.sys_deleted = 0 AND r.sys_deleted = 0 "
        + "ORDER BY r.report_month DESC, e.sequence_no";
    return jdbc.query(sql, (rs, row) -> history(rs), args.toArray());
  }

  @Override
  public List<ExportProgressEntry> findProgressEntries(
      List<Long> studyIds, LocalDate startDate, LocalDate endDate) {
    if (studyIds == null || studyIds.isEmpty()) {
      return List.of();
    }
    StringJoiner placeholders = new StringJoiner(", ");
    List<Object> args = new ArrayList<>();
    for (Long studyId : studyIds) {
      placeholders.add("?");
      args.add(studyId);
    }
    args.add(Date.valueOf(startDate));
    args.add(Date.valueOf(endDate));
    String sql = """
        SELECT r.study_id,
          COALESCE(r.study_code_snapshot, '') AS study_code,
          COALESCE(r.program_code_snapshot, '') AS program_code,
          COALESCE(r.therapeutic_area_name_snapshot, '') AS ta_name,
          e.entry_date,
          r.function_line_code_snapshot AS function_code,
          r.function_line_name_snapshot AS function_name,
          e.progress_content
        FROM hd_plt_monthly_report_entry e
        JOIN hd_plt_monthly_report r ON r.id = e.monthly_report_id
        WHERE r.study_id IN (""" + placeholders + """
        )
          AND e.entry_date >= ? AND e.entry_date <= ?
          AND e.sys_deleted = 0 AND r.sys_deleted = 0
          AND TRIM(e.progress_content) <> ''
        ORDER BY e.entry_date, r.study_code_snapshot, r.function_line_code_snapshot, e.sequence_no
        """;
    return jdbc.query(sql, (rs, row) -> new ExportProgressEntry(
        rs.getLong("study_id"),
        rs.getString("study_code"),
        rs.getString("program_code"),
        rs.getString("ta_name"),
        rs.getDate("entry_date").toLocalDate(),
        rs.getString("function_code"),
        rs.getString("function_name"),
        rs.getString("progress_content")), args.toArray());
  }

  // ──────────── helpers ────────────

  private void audit(long operatorUserId, String operatorEmail, long entryId,
                     String reason, String actionCode) {
    jdbc.update("""
        INSERT INTO hd_plt_audit_log(
          operator_user_id, operator_email, action_code, target_table, target_id,
          operation_reason, result_code)
        VALUES (?, ?, ?, 'hd_plt_monthly_report_entry', ?, ?, 'SUCCESS')
        """, operatorUserId, operatorEmail, actionCode, entryId, reason);
  }

  private static final org.springframework.jdbc.core.RowMapper<FunctionLineRef>
      FUNCTION_LINE_MAPPER = (rs, row) -> new FunctionLineRef(
          rs.getLong("id"), rs.getString("function_code"), rs.getString("function_name"));

  private static PersistedMonthlyReport report(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new PersistedMonthlyReport(
        rs.getLong("id"), rs.getLong("study_id"),
        rs.getDate("report_month").toLocalDate(),
        rs.getLong("function_line_id"), rs.getString("function_line_code_snapshot"),
        rs.getString("function_line_name_snapshot"));
  }

  private static PersistedMonthlyEntry entry(java.sql.ResultSet rs) throws java.sql.SQLException {
    Timestamp updatedAt = rs.getTimestamp("sys_update_time");
    return new PersistedMonthlyEntry(
        rs.getLong("id"), rs.getLong("monthly_report_id"),
        rs.getDate("entry_date").toLocalDate(), rs.getInt("sequence_no"),
        rs.getString("progress_content"), rs.getString("sys_update_by"),
        updatedAt == null ? null : Instant.ofEpochMilli(updatedAt.getTime()));
  }

  private static HistoryEntry history(java.sql.ResultSet rs) throws java.sql.SQLException {
    Timestamp updatedAt = rs.getTimestamp("sys_update_time");
    return new HistoryEntry(
        rs.getLong("id"), rs.getDate("report_month").toLocalDate(),
        rs.getDate("entry_date").toLocalDate(), rs.getString("progress_content"),
        rs.getString("sys_update_by"),
        updatedAt == null ? null : Instant.ofEpochMilli(updatedAt.getTime()));
  }
}
