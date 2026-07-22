package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.MilestoneSaveCommand;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MilestoneRepositoryIntegrationTest {

  @Autowired StudyMilestonePort milestones;
  @Autowired JdbcTemplate jdbc;

  @Test
  void saveUpsertsMilestoneRowAndWritesAuditLogWithRealColumns() {
    long studyId = seedStudy();

    milestones.save(new MilestoneSaveCommand(
        studyId, "IND", "IND-0",
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 1, 5), null, "提前启动", "tester@example.com"));

    // Exercise the ON DUPLICATE KEY UPDATE branch (the path that previously threw
    // "The getKey method should only be used when a single key is returned").
    milestones.save(new MilestoneSaveCommand(
        studyId, "IND", "IND-0",
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1),
        LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20), "已调整", "tester@example.com"));

    var rows = jdbc.queryForList(
        "SELECT milestone_code, deviation_note FROM hd_plt_study_milestone "
            + "WHERE study_id = ? AND sys_deleted = 0", studyId);
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).get("milestone_code")).isEqualTo("IND-0");
    assertThat(rows.get(0).get("deviation_note")).isEqualTo("已调整");

    var audit = jdbc.queryForList(
        "SELECT action_code, target_table, operator_email, result_code "
            + "FROM hd_plt_audit_log WHERE target_table = 'hd_plt_study_milestone' "
            + "AND action_code = 'MILESTONE_SAVE'");
    assertThat(audit).hasSize(2);
    assertThat(audit.get(0).get("operator_email")).isEqualTo("tester@example.com");
    assertThat(audit.get(0).get("result_code")).isEqualTo("SUCCESS");
  }

  private long seedStudy() {
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(area_code, area_name, status_code,
          sys_create_by, sys_update_by)
        SELECT 'ONCOLOGY', '肿瘤', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (
          SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'ONCOLOGY')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_program(program_code, product_name, status_code,
          sys_create_by, sys_update_by)
        SELECT 'PROGRAM-001', 'HD-001', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (
          SELECT 1 FROM hd_plt_program WHERE program_code = 'PROGRAM-001')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_project(project_code, program_id, indication_description,
          therapeutic_area_id, sys_create_by, sys_update_by)
        SELECT 'PROJECT-001', p.id, '实体瘤', ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = 'PROGRAM-001' AND ta.area_code = 'ONCOLOGY'
          AND NOT EXISTS (
            SELECT 1 FROM hd_plt_project WHERE project_code = 'PROJECT-001')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_study(
          study_code, program_id, program_code_snapshot, product_name_snapshot,
          project_id, project_code_snapshot,
          therapeutic_area_id, therapeutic_area_code_snapshot, therapeutic_area_name_snapshot,
          indication_description_snapshot, sys_create_by, sys_update_by)
        SELECT 'MS-001',
          (SELECT id FROM hd_plt_program WHERE program_code = 'PROGRAM-001'),
          'PROGRAM-001', 'HD-001',
          (SELECT id FROM hd_plt_project WHERE project_code = 'PROJECT-001'),
          'PROJECT-001',
          (SELECT id FROM hd_plt_therapeutic_area WHERE area_code = 'ONCOLOGY'),
          'ONCOLOGY', '肿瘤', '实体瘤', 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_study WHERE study_code = 'MS-001')
        """);
    return jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = 'MS-001'", Long.class);
  }
}
