package com.huadong.pipeline.repository;


import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMilestoneRepository implements StudyMilestonePort {

  @Autowired
  private JdbcTemplate jdbc;

  @Override
  public Optional<StudyRef> findStudy(StudyAccessScope scope, long studyId) {
    List<Object> args = new ArrayList<>();
    args.add(studyId);
    String scopeSql = scopeClause(scope, args);
    return jdbc.query("""
        SELECT s.id, s.study_code, s.program_id, s.program_code_snapshot,
          s.project_id, s.project_code_snapshot
        FROM hd_plt_study s WHERE s.id = ? AND s.sys_deleted = 0
        """ + scopeSql, (rs, row) -> new StudyRef(
            rs.getLong("id"), rs.getString("study_code"),
            rs.getLong("program_id"), rs.getString("program_code_snapshot"),
            rs.getLong("project_id"), rs.getString("project_code_snapshot")),
        args.toArray()).stream().findFirst();
  }

  @Override
  public List<PersistedMilestone> findByStudyId(long studyId) {
    return jdbc.query("""
        SELECT id, study_id, stage_code, milestone_code,
          plan_v1_date, plan_v2_date, actual_start_date, actual_end_date, deviation_note
        FROM hd_plt_study_milestone
        WHERE study_id = ? AND sys_deleted = 0
        ORDER BY id
        """, (rs, row) -> new PersistedMilestone(
            rs.getLong("id"), rs.getLong("study_id"),
            rs.getString("stage_code"), rs.getString("milestone_code"),
            localDate(rs.getDate("plan_v1_date")), localDate(rs.getDate("plan_v2_date")),
            localDate(rs.getDate("actual_start_date")), localDate(rs.getDate("actual_end_date")),
            rs.getString("deviation_note")), studyId);
  }

  @Override
  public List<PersistedMilestone> findByStudyIds(List<Long> studyIds) {
    if (studyIds == null || studyIds.isEmpty()) {
      return List.of();
    }
    String placeholders = studyIds.stream().map(id -> "?").collect(java.util.stream.Collectors.joining(","));
    String sql = """
        SELECT id, study_id, stage_code, milestone_code,
          plan_v1_date, plan_v2_date, actual_start_date, actual_end_date, deviation_note
        FROM hd_plt_study_milestone
        WHERE study_id IN (%s) AND sys_deleted = 0
        ORDER BY study_id, id
        """.formatted(placeholders);
    return jdbc.query(sql, (rs, row) -> new PersistedMilestone(
        rs.getLong("id"), rs.getLong("study_id"),
        rs.getString("stage_code"), rs.getString("milestone_code"),
        localDate(rs.getDate("plan_v1_date")), localDate(rs.getDate("plan_v2_date")),
        localDate(rs.getDate("actual_start_date")), localDate(rs.getDate("actual_end_date")),
        rs.getString("deviation_note")), studyIds.toArray());
  }

  @Override
  public PersistedMilestone save(MilestoneSaveCommand command) {
    // Upsert a single milestone node. We deliberately avoid GeneratedKeyHolder here:
    // with INSERT ... ON DUPLICATE KEY UPDATE the MySQL driver returns the generated
    // key for BOTH the insert and the update-hit branches, so getKey() throws
    // "The current key entry contains multiple keys". Instead we read the id back
    // via the unique (study_id, milestone_code) pair, which is correct for both branches.
    jdbc.update("""
        INSERT INTO hd_plt_study_milestone(
          study_id, stage_code, milestone_code,
          plan_v1_date, plan_v2_date, actual_start_date, actual_end_date,
          deviation_note, sys_create_by, sys_update_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
          plan_v1_date = VALUES(plan_v1_date),
          plan_v2_date = VALUES(plan_v2_date),
          actual_start_date = VALUES(actual_start_date),
          actual_end_date = VALUES(actual_end_date),
          deviation_note = VALUES(deviation_note),
          sys_update_by = VALUES(sys_update_by),
          sys_update_time = CURRENT_TIMESTAMP(6)
        """,
        command.studyId(), command.stageCode(), command.milestoneCode(),
        date(command.planV1Date()), date(command.planV2Date()),
        date(command.actualStartDate()), date(command.actualEndDate()),
        command.deviationNote(), command.operatorEmail(), command.operatorEmail());

    long id = jdbc.queryForObject(
        "SELECT id FROM hd_plt_study_milestone WHERE study_id = ? AND milestone_code = ? AND sys_deleted = 0",
        Long.class, command.studyId(), command.milestoneCode());

    audit("MILESTONE_SAVE", command);

    return new PersistedMilestone(
        id, command.studyId(), command.stageCode(), command.milestoneCode(),
        command.planV1Date(), command.planV2Date(),
        command.actualStartDate(), command.actualEndDate(),
        command.deviationNote());
  }

  // ──────────── helpers ────────────

  private static String scopeClause(StudyAccessScope scope, List<Object> args) {
    if (scope.allStudies()) {
      return "";
    }
    args.add(scope.userId());
    return " AND EXISTS (SELECT 1 FROM hd_plt_team_assignment scope_ta"
        + " WHERE scope_ta.study_id = s.id AND scope_ta.user_id = ?"
        + " AND scope_ta.sys_deleted = 0)";
  }

  private void audit(String action, MilestoneSaveCommand command) {
    jdbc.update("""
        INSERT INTO hd_plt_audit_log(
          operator_user_id, operator_email, action_code, target_table, target_id,
          operation_reason, result_code)
        VALUES (NULL, ?, ?, 'hd_plt_study_milestone', NULL, ?, 'SUCCESS')
        """, command.operatorEmail(), action,
        command.studyId() + "/" + command.milestoneCode()
            + " V1=" + command.planV1Date() + " V2=" + command.planV2Date()
            + " AS=" + command.actualStartDate() + " AE=" + command.actualEndDate()
            + " note=" + command.deviationNote());
  }

  private static Date date(LocalDate value) {
    return value == null ? null : Date.valueOf(value);
  }

  private static LocalDate localDate(Date value) {
    return value == null ? null : value.toLocalDate();
  }
}
