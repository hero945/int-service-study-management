package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MonthlyExportIntegrationTest {

  private static final String MEMBER = "monthly.export@example.com";

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void previewFiltersByScopeAndDateRangeOnlyAffectsProgress() throws Exception {
    long oncologyStudy = seedStudy("MS-EXP-001", "PROGRAM-EXP-1", "PROJECT-EXP-1",
        "ONCOLOGY", "肿瘤", "实体瘤");
    long autoimmuneStudy = seedStudy("MS-EXP-002", "PROGRAM-EXP-2", "PROJECT-EXP-2",
        "AUTOIMMUNE", "自身免疫", "SLE");
    long memberId = seedMonthlyUser(MEMBER, "导出成员");
    seedAssignment(oncologyStudy, memberId, "CM");
    seedAssignment(autoimmuneStudy, memberId, "CM");

    seedEntry(oncologyStudy, "2026-07", "2026-07-10", "七月肿瘤进展");
    seedEntry(oncologyStudy, "2026-06", "2026-06-20", "六月肿瘤进展");
    seedEntry(autoimmuneStudy, "2026-07", "2026-07-12", "七月免疫进展");

    long oncologyTaId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_therapeutic_area WHERE area_code = 'ONCOLOGY'", Long.class);

    mvc.perform(get("/api/v1/reports/monthly/preview")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "TA")
            .param("taIds", String.valueOf(oncologyTaId))
            .with(user(MEMBER).authorities(authority("report.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.scopeType").value("TA"))
        .andExpect(jsonPath("$.summary.total").value(1))
        .andExpect(jsonPath("$.snapshotGroups.length()").value(1))
        .andExpect(jsonPath("$.snapshotGroups[0].rows[0].studyCode").value("MS-EXP-001"))
        .andExpect(jsonPath("$.progress.length()").value(1))
        .andExpect(jsonPath("$.progress[0].content").value("七月肿瘤进展"))
        .andExpect(jsonPath("$.progress[0].studyCode").value("MS-EXP-001"))
        .andExpect(jsonPath("$.summary.reportedStudyCount").value(1));

    // Wider date range includes June progress for the same TA-scoped study set.
    mvc.perform(get("/api/v1/reports/monthly/preview")
            .param("startDate", "2026-06-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "TA")
            .param("taIds", String.valueOf(oncologyTaId))
            .with(user(MEMBER).authorities(authority("report.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summary.total").value(1))
        .andExpect(jsonPath("$.progress.length()").value(2));
  }

  @Test
  void exportHtmlCsvAndXlsx() throws Exception {
    long studyId = seedStudy("MS-EXP-010", "PROGRAM-EXP-10", "PROJECT-EXP-10",
        "ONCOLOGY", "肿瘤", "实体瘤");
    long memberId = seedMonthlyUser(MEMBER, "导出成员");
    seedAssignment(studyId, memberId, "CM");
    seedEntry(studyId, "2026-07", "2026-07-15", "导出进展样本");

    MvcResult html = mvc.perform(get("/api/v1/reports/monthly/export")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "ALL")
            .param("format", "html")
            .with(user(MEMBER).authorities(authority("report.export"))))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/html")))
        .andReturn();
    assertThat(new String(html.getResponse().getContentAsByteArray(), java.nio.charset.StandardCharsets.UTF_8))
        .contains("临床研发管线月度报告")
        .contains("导出进展样本");

    MvcResult csv = mvc.perform(get("/api/v1/reports/monthly/export")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "ALL")
            .param("format", "csv")
            .with(user(MEMBER).authorities(authority("report.export"))))
        .andExpect(status().isOk())
        .andReturn();
    assertThat(new String(csv.getResponse().getContentAsByteArray(), java.nio.charset.StandardCharsets.UTF_8))
        .contains("导出进展样本");

    MvcResult xlsx = mvc.perform(get("/api/v1/reports/monthly/export")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "ALL")
            .param("format", "xlsx")
            .with(user(MEMBER).authorities(authority("report.export"))))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type",
            org.hamcrest.Matchers.containsString("spreadsheetml")))
        .andReturn();
    assertThat(xlsx.getResponse().getContentAsByteArray().length).isGreaterThan(100);
  }

  @Test
  void programScopeRequiresSelection() throws Exception {
    mvc.perform(get("/api/v1/reports/monthly/preview")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "PROGRAM")
            .with(user("admin@example.com").authorities(authority("report.page.view"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_SCOPE"));
  }

  @Test
  void exportRequiresExportAuthority() throws Exception {
    mvc.perform(get("/api/v1/reports/monthly/export")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "ALL")
            .param("format", "html")
            .with(user(MEMBER).authorities(authority("report.page.view"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void previewUsesMilestoneDerivedStudyStatus() throws Exception {
    long studyId = seedStudy("MS-EXP-STATUS", "PROGRAM-EXP-S", "PROJECT-EXP-S",
        "ONCOLOGY", "肿瘤", "实体瘤");
    long memberId = seedMonthlyUser(MEMBER, "导出成员");
    seedAssignment(studyId, memberId, "CM");

    mvc.perform(get("/api/v1/reports/monthly/preview")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "ALL")
            .with(user(MEMBER).authorities(authority("report.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summary.notStarted").value(1))
        .andExpect(jsonPath("$.summary.inProgress").value(0))
        .andExpect(jsonPath("$.summary.completed").value(0))
        .andExpect(jsonPath("$.snapshotGroups[0].rows[0].projectStatus").value("未开始"));

    jdbc.update("""
        INSERT INTO hd_plt_study_milestone(
          study_id, stage_code, milestone_code,
          actual_start_date, actual_end_date, sys_create_by, sys_update_by)
        VALUES (?, 'PreIND', 'PreIND-0', DATE '2026-01-01', DATE '2026-01-02', 'seed', 'seed')
        """, studyId);

    mvc.perform(get("/api/v1/reports/monthly/preview")
            .param("startDate", "2026-07-01")
            .param("endDate", "2026-07-31")
            .param("scopeType", "ALL")
            .with(user(MEMBER).authorities(authority("report.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summary.notStarted").value(0))
        .andExpect(jsonPath("$.summary.inProgress").value(1))
        .andExpect(jsonPath("$.summary.completed").value(0))
        .andExpect(jsonPath("$.snapshotGroups[0].rows[0].projectStatus").value("进行中"));
  }

  private void seedEntry(long studyId, String month, String entryDate, String content)
      throws Exception {
    mvc.perform(get("/api/v1/studies/{id}/monthly-reports", studyId)
            .param("month", month)
            .with(user(MEMBER).authorities(authority("monthly.read"))))
        .andExpect(status().isOk());
    long reportId = jdbc.queryForObject("""
        SELECT mr.id FROM hd_plt_monthly_report mr
        JOIN hd_plt_function_line fl ON fl.id = mr.function_line_id
        WHERE mr.study_id = ? AND fl.function_code = 'CM' AND mr.report_month = ?
          AND mr.sys_deleted = 0
        """, Long.class, studyId, Date.valueOf(month + "-01"));
    mvc.perform(post("/api/v1/monthly-reports/{reportId}/entries", reportId)
            .with(user(MEMBER).authorities(authority("monthly.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"entryDate\":\"" + entryDate + "\",\"content\":\"" + content + "\"}"))
        .andExpect(status().isOk());
  }

  private long seedMonthlyUser(String email, String displayName) {
    long userId = seedUser(email, displayName);
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code,
            is_system_role, sys_create_by, sys_update_by)
        SELECT 'MONTHLY_EXPORT_SCOPED', 'Assigned-study monthly export role', 'ASSIGNED_STUDY',
            'ACTIVE', 0, 'seed', 'seed'
        WHERE NOT EXISTS (
            SELECT 1 FROM hd_plt_role WHERE role_name = 'MONTHLY_EXPORT_SCOPED')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
        SELECT r.id, p.id, 'seed', 'seed'
        FROM hd_plt_role r JOIN hd_plt_permission p ON p.permission_code IN (
            'monthly.read', 'monthly.create', 'monthly.update')
        WHERE r.role_name = 'MONTHLY_EXPORT_SCOPED'
          AND NOT EXISTS (
            SELECT 1 FROM hd_plt_role_permission rp
            WHERE rp.role_id = r.id AND rp.permission_id = p.id)
        """);
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT ?, id, 'seed', 'seed'
        FROM hd_plt_role WHERE role_name = 'MONTHLY_EXPORT_SCOPED'
        """, userId);
    return userId;
  }

  private long seedUser(String email, String displayName) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', ?, 'seed', 'seed')
        """, email, displayName, UUID.randomUUID().toString());
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
        WHERE fl.function_code = ? AND tr.role_code = 'CM'
        """, studyId, userId, functionCode);
  }

  private long seedStudy(String studyCode, String programCode, String projectCode,
                         String taCode, String taName, String indication) {
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(area_code, area_name, status_code,
          sys_create_by, sys_update_by)
        SELECT ?, ?, 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (
          SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = ?)
        """, taCode, taName, taCode);
    jdbc.update("""
        INSERT INTO hd_plt_program(program_code, product_name, status_code,
          sys_create_by, sys_update_by)
        SELECT ?, ?, 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (
          SELECT 1 FROM hd_plt_program WHERE program_code = ?)
        """, programCode, "Product-" + programCode, programCode);
    jdbc.update("""
        INSERT INTO hd_plt_project(project_code, program_id, indication_description,
          therapeutic_area_id, sys_create_by, sys_update_by)
        SELECT ?, p.id, ?, ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = ? AND ta.area_code = ?
          AND NOT EXISTS (
            SELECT 1 FROM hd_plt_project WHERE project_code = ?)
        """, projectCode, indication, programCode, taCode, projectCode);
    jdbc.update("""
        INSERT INTO hd_plt_study(
          study_code, program_id, program_code_snapshot, product_name_snapshot,
          project_id, project_code_snapshot,
          therapeutic_area_id, therapeutic_area_code_snapshot, therapeutic_area_name_snapshot,
          indication_description_snapshot, sys_create_by, sys_update_by)
        SELECT ?,
          (SELECT id FROM hd_plt_program WHERE program_code = ?),
          ?, ?,
          (SELECT id FROM hd_plt_project WHERE project_code = ?),
          ?,
          (SELECT id FROM hd_plt_therapeutic_area WHERE area_code = ?),
          ?, ?, ?, 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_study WHERE study_code = ?)
        """, studyCode, programCode, programCode, "Product-" + programCode,
        projectCode, projectCode, taCode, taCode, taName, indication, studyCode);
    return jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = ?", Long.class, studyCode);
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
