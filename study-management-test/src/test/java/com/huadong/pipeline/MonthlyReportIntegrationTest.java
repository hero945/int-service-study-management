package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MonthlyReportIntegrationTest {

  private static final String MONTH = "2026-07";
  private static final String MEMBER = "monthly.member@example.com";

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void getMaterializesIdempotentlyAndScopesVisibilityToAssignments() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long otherId = seedUser("other.member@example.com", "其他成员", true);
    seedAssignment(studyId, otherId, "ST");

    // 普通用户只看到自己被分配的功能线（CM）
    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", MONTH)
            .with(user(MEMBER).authorities(authority("monthly.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studyId").value(studyId))
        .andExpect(jsonPath("$.studyCode").value("MS-001"))
        .andExpect(jsonPath("$.month").value(MONTH))
        .andExpect(jsonPath("$.functionLines.length()").value(1))
        .andExpect(jsonPath("$.functionLines[0].functionCode").value("CM"))
        .andExpect(jsonPath("$.functionLines[0].functionName").value("临床医学"))
        .andExpect(jsonPath("$.functionLines[0].editable").value(true))
        .andExpect(jsonPath("$.functionLines[0].entries.length()").value(0));
    assertThat(reportCount(studyId)).isEqualTo(1);

    // 第二次调用幂等：应填项行数不变
    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", MONTH)
            .with(user(MEMBER).authorities(authority("monthly.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLines.length()").value(1));
    assertThat(reportCount(studyId)).isEqualTo(1);

    // 管理员可填全部功能线：看到库中全部 ACTIVE 功能线（不限该 study 的团队分配）
    int activeLines = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_function_line WHERE status_code = 'ACTIVE'", Integer.class);
    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", MONTH)
            .with(user("admin@example.com").authorities(authority("monthly.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLines.length()").value(activeLines))
        .andExpect(jsonPath("$.functionLines[0].editable").value(true));
    assertThat(reportCount(studyId)).isEqualTo(activeLines);

    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", MONTH)
            .with(user("admin@example.com").authorities(authority("monthly.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLines.length()").value(activeLines));
    assertThat(reportCount(studyId)).isEqualTo(activeLines);
  }

  @Test
  void postCreatesIndependentEntriesAndIncrementsSequenceWithoutOverwriting() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long reportId = materialize(studyId);

    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-20\",\"content\":\"第一次进展\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLines[0].entries.length()").value(1))
        .andExpect(jsonPath("$.functionLines[0].entries[0].entryDate").value("2026-07-20"))
        .andExpect(jsonPath("$.functionLines[0].entries[0].content").value("第一次进展"))
        .andExpect(jsonPath("$.functionLines[0].entries[0].updatedBy").value(MEMBER))
        .andExpect(jsonPath("$.functionLines[0].entries[0].updatedAt").isString())
        .andExpect(jsonPath("$.functionLines[0].entries[0].editable").value(true));

    // 同月第二次汇报：独立成行，不覆盖第一次
    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-21\",\"content\":\"第二次进展\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLines[0].entries.length()").value(2))
        .andExpect(jsonPath("$.functionLines[0].entries[0].content").value("第一次进展"))
        .andExpect(jsonPath("$.functionLines[0].entries[1].content").value("第二次进展"));

    List<Integer> sequences = jdbc.queryForList("""
        SELECT sequence_no FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sys_deleted = 0 ORDER BY sequence_no
        """, Integer.class, reportId);
    assertThat(sequences).containsExactly(1, 2);
  }

  @Test
  void patchUpdatesOnlyTheTargetEntry() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long reportId = materialize(studyId);

    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-20\",\"content\":\"原始内容\"}"))
        .andExpect(status().isOk());
    long entryId = jdbc.queryForObject("""
        SELECT id FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sys_deleted = 0
        """, Long.class, reportId);

    mvc.perform(patch("/api/v1/monthly-report-entries/{entryId}", entryId)
            .with(user(MEMBER).authorities(authority("monthly.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-22\",\"content\":\"修订后内容\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLines[0].entries.length()").value(1))
        .andExpect(jsonPath("$.functionLines[0].entries[0].entryDate").value("2026-07-22"))
        .andExpect(jsonPath("$.functionLines[0].entries[0].content").value("修订后内容"));

    var row = jdbc.queryForMap("""
        SELECT progress_content, entry_date FROM hd_plt_monthly_report_entry
        WHERE id = ? AND sys_deleted = 0
        """, entryId);
    assertThat(row.get("progress_content")).isEqualTo("修订后内容");
    assertThat(row.get("entry_date").toString()).isEqualTo("2026-07-22");
  }

  @Test
  void postWithoutMatchingFunctionLineAssignmentIsForbidden() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long reportId = materialize(studyId);

    // 有 monthly.* 权限但只被分配到 PV 功能线的用户 → 对象级规则拒绝
    long outsiderId = seedMonthlyUser("monthly.outsider@example.com", "无关成员");
    seedAssignment(studyId, outsiderId, "PV");

    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user("monthly.outsider@example.com")
                .authorities(authority("monthly.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-20\",\"content\":\"不应写入\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("MONTHLY_FORBIDDEN"));

    Integer entries = jdbc.queryForObject("""
        SELECT COUNT(*) FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sys_deleted = 0
        """, Integer.class, reportId);
    assertThat(entries).isZero();
  }

  @Test
  void writesAuditRowsWithRealColumnsForCreateAndUpdate() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long reportId = materialize(studyId);

    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-20\",\"content\":\"第一次进展\"}"))
        .andExpect(status().isOk());
    long entryId = jdbc.queryForObject("""
        SELECT id FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sys_deleted = 0
        """, Long.class, reportId);
    mvc.perform(patch("/api/v1/monthly-report-entries/{entryId}", entryId)
            .with(user(MEMBER).authorities(authority("monthly.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-21\",\"content\":\"修订\"}"))
        .andExpect(status().isOk());

    var audits = jdbc.queryForList("""
        SELECT action_code, target_table, target_id, operator_email, operation_reason,
          result_code
        FROM hd_plt_audit_log
        WHERE target_table = 'hd_plt_monthly_report_entry' AND action_code = 'monthly_save'
        ORDER BY id
        """);
    assertThat(audits).hasSize(2);
    assertThat(audits.get(0).get("target_id")).isEqualTo(entryId);
    assertThat(audits.get(0).get("operator_email")).isEqualTo(MEMBER);
    assertThat(audits.get(0).get("result_code")).isEqualTo("SUCCESS");
    assertThat((String) audits.get(0).get("operation_reason"))
        .contains("studyId=" + studyId).contains("month=2026-07")
        .contains("functionCode=CM").contains("isNew=true");
    assertThat((String) audits.get(1).get("operation_reason")).contains("isNew=false");
  }

  @Test
  void deleteSoftRemovesEntryWritesAuditAndReassemblesPage() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long reportId = materialize(studyId);

    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-20\",\"content\":\"待删除\"}"))
        .andExpect(status().isOk());
    long entryId = jdbc.queryForObject("""
        SELECT id FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sys_deleted = 0
        """, Long.class, reportId);

    mvc.perform(delete("/api/v1/monthly-report-entries/{entryId}", entryId)
            .with(user(MEMBER).authorities(authority("monthly.update"))).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLines[0].entries.length()").value(0));

    // 软删：行仍在但 sys_deleted = 1
    Integer deleted = jdbc.queryForObject("""
        SELECT COUNT(*) FROM hd_plt_monthly_report_entry
        WHERE id = ? AND sys_deleted = 1
        """, Integer.class, entryId);
    assertThat(deleted).isOne();

    // 审计以 monthly_delete 记录
    var audits = jdbc.queryForList("""
        SELECT action_code, target_id FROM hd_plt_audit_log
        WHERE target_table = 'hd_plt_monthly_report_entry' AND target_id = ?
          AND action_code = 'monthly_delete'
        """, entryId);
    assertThat(audits).hasSize(1);
  }

  @Test
  void deleteWithoutFunctionLineAssignmentIsForbidden() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long reportId = materialize(studyId);

    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"2026-07-20\",\"content\":\"别人的\"}"))
        .andExpect(status().isOk());
    long entryId = jdbc.queryForObject("""
        SELECT id FROM hd_plt_monthly_report_entry
        WHERE monthly_report_id = ? AND sys_deleted = 0
        """, Long.class, reportId);

    long outsiderId = seedMonthlyUser("monthly.outsider@example.com", "无关成员");
    seedAssignment(studyId, outsiderId, "PV");

    mvc.perform(delete("/api/v1/monthly-report-entries/{entryId}", entryId)
            .with(user("monthly.outsider@example.com")
                .authorities(authority("monthly.update"))).with(csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("MONTHLY_FORBIDDEN"));

    Integer alive = jdbc.queryForObject("""
        SELECT COUNT(*) FROM hd_plt_monthly_report_entry
        WHERE id = ? AND sys_deleted = 0
        """, Integer.class, entryId);
    assertThat(alive).isOne();
  }

  @Test
  void historyReturnsPreviousTwoMonthsAndHandlesYearBoundary() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");

    // 当前月 2026-01，前两个月应为 2025-12 与 2025-11（跨年边界）
    String current = "2026-01";
    seedHistory(studyId, "2025-12", "CM", "十二月进展");
    seedHistory(studyId, "2025-11", "CM", "十一月进展");

    long cmLine = jdbc.queryForObject(
        "SELECT id FROM hd_plt_function_line WHERE function_code = 'CM'", Long.class);

    mvc.perform(get("/api/v1/studies/{id}/monthly-reports/history", studyId)
            .param("functionLineId", String.valueOf(cmLine))
            .param("month", current)
            .with(user(MEMBER).authorities(authority("monthly.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.functionLineId").value(cmLine))
        .andExpect(jsonPath("$.functionCode").value("CM"))
        .andExpect(jsonPath("$.months.length()").value(2))
        .andExpect(jsonPath("$.months[0].month").value("2025-12"))
        .andExpect(jsonPath("$.months[0].entries.length()").value(1))
        .andExpect(jsonPath("$.months[0].entries[0].content").value("十二月进展"))
        .andExpect(jsonPath("$.months[1].month").value("2025-11"))
        .andExpect(jsonPath("$.months[1].entries.length()").value(1))
        .andExpect(jsonPath("$.months[1].entries[0].content").value("十一月进展"));
  }

  @Test
  void historyWithoutFunctionLineAssignmentIsForbidden() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");
    long cmLine = jdbc.queryForObject(
        "SELECT id FROM hd_plt_function_line WHERE function_code = 'CM'", Long.class);

    long outsiderId = seedMonthlyUser("monthly.outsider@example.com", "无关成员");
    seedAssignment(studyId, outsiderId, "PV");

    mvc.perform(get("/api/v1/studies/{id}/monthly-reports/history", studyId)
            .param("functionLineId", String.valueOf(cmLine))
            .param("month", MONTH)
            .with(user("monthly.outsider@example.com")
                .authorities(authority("monthly.read"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("MONTHLY_FORBIDDEN"));
  }

  @Test
  void getMonthlyReportsWithoutStudyAssignmentIsForbidden() throws Exception {
    long studyId = seedStudy();
    seedMonthlyUser("monthly.unassigned@example.com", "无分配成员");

    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", MONTH)
            .with(user("monthly.unassigned@example.com")
                .authorities(authority("monthly.read"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("STUDY_OUT_OF_SCOPE"));
  }

  @Test
  void getMonthlyReportsWithinAssignedStudyScopeIsAllowed() throws Exception {
    long studyId = seedStudy();
    long memberId = seedMonthlyUser(MEMBER, "月报成员");
    seedAssignment(studyId, memberId, "CM");

    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", MONTH)
            .with(user(MEMBER).authorities(authority("monthly.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studyId").value(studyId))
        .andExpect(jsonPath("$.studyCode").value("MS-001"));
  }

  // ──────────── seed helpers ────────────

  /** GET as member to trigger materialization, then read back the CM report id. */
  private long materialize(long studyId) throws Exception {
    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", MONTH)
            .with(user(MEMBER).authorities(authority("monthly.read"))))
        .andExpect(status().isOk());
    return jdbc.queryForObject("""
        SELECT mr.id FROM hd_plt_monthly_report mr
        JOIN hd_plt_function_line fl ON fl.id = mr.function_line_id
        WHERE mr.study_id = ? AND fl.function_code = 'CM' AND mr.sys_deleted = 0
        """, Long.class, studyId);
  }

  /** Materialize the given month for the member, then POST one entry. */
  private void seedHistory(long studyId, String month, String functionCode, String content) throws Exception {
    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", month)
            .with(user(MEMBER).authorities(authority("monthly.read"))))
        .andExpect(status().isOk());
    long reportId = jdbc.queryForObject("""
        SELECT mr.id FROM hd_plt_monthly_report mr
        JOIN hd_plt_function_line fl ON fl.id = mr.function_line_id
        WHERE mr.study_id = ? AND fl.function_code = ? AND mr.report_month = ?
          AND mr.sys_deleted = 0
        """, Long.class, studyId, functionCode, Date.valueOf(month + "-01"));
    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"" + month + "-15\",\"content\":\"" + content + "\"}"))
        .andExpect(status().isOk());
  }

  private int reportCount(long studyId) {
    Integer count = jdbc.queryForObject("""
        SELECT COUNT(*) FROM hd_plt_monthly_report
        WHERE study_id = ? AND report_month = DATE '2026-07-01' AND sys_deleted = 0
        """, Integer.class, studyId);
    return count == null ? 0 : count;
  }

  private long seedMonthlyUser(String email, String displayName) {
    long userId = seedUser(email, displayName, true);
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code,
            is_system_role, sys_create_by, sys_update_by)
        SELECT 'MONTHLY_SCOPED', 'Assigned-study monthly role', 'ASSIGNED_STUDY',
            'ACTIVE', 0, 'seed', 'seed'
        WHERE NOT EXISTS (
            SELECT 1 FROM hd_plt_role WHERE role_name = 'MONTHLY_SCOPED')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
        SELECT r.id, p.id, 'seed', 'seed'
        FROM hd_plt_role r JOIN hd_plt_permission p ON p.permission_code IN (
            'monthly.read', 'monthly.create', 'monthly.update')
        WHERE r.role_name = 'MONTHLY_SCOPED'
          AND NOT EXISTS (
            SELECT 1 FROM hd_plt_role_permission rp
            WHERE rp.role_id = r.id AND rp.permission_id = p.id)
        """);
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT ?, id, 'seed', 'seed'
        FROM hd_plt_role WHERE role_name = 'MONTHLY_SCOPED'
        """, userId);
    return userId;
  }

  private long seedUser(String email, String displayName, boolean enabled) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, ?, ?, 'seed', 'seed')
        """, email, displayName, enabled ? "ACTIVE" : "DISABLED", UUID.randomUUID().toString());
    return jdbc.queryForObject("SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);
  }

  private void seedAssignment(long studyId, long userId, String functionCode) {
    jdbc.update("""
        INSERT INTO hd_plt_team_assignment(
            study_id, team_role_id, user_id,
            team_role_code_snapshot, team_role_name_snapshot,
            user_email_snapshot, user_name_snapshot,
            function_line_id, function_line_code_snapshot, function_line_name_snapshot,
            sys_create_by, sys_update_by)
        SELECT ?, tr.id, u.id,
            tr.role_code, tr.role_name,
            u.email, u.display_name,
            fl.id, fl.function_code, fl.function_name,
            'seed', 'seed'
        FROM hd_plt_function_line fl
        JOIN hd_plt_team_role tr ON tr.function_line_id = fl.id
        JOIN hd_plt_user u ON u.id = ?
        WHERE fl.function_code = ? AND tr.role_code = ?
        """, studyId, userId, functionCode, teamRoleCode(functionCode));
  }

  private static String teamRoleCode(String functionCode) {
    return switch (functionCode) {
      case "CM" -> "CM";
      case "ST" -> "ST";
      case "PV" -> "PVP";
      default -> throw new IllegalArgumentException("unexpected function code " + functionCode);
    };
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

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
