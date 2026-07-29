package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huadong.pipeline.domain.audit.AuditContext;
import com.huadong.pipeline.domain.audit.AuditEvent;
import com.huadong.pipeline.domain.audit.AuditLogRepository;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuditLogIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired AuditLogRepository auditLogs;

  @Test
  void unifiedRepositoryRoundTripsJsonAndResponseRemovesSensitiveFields() throws Exception {
    auditLogs.insert(new AuditEvent(
        "ACCOUNT", "USER", 9L, "safe@example.com", null, null, null, null,
        "USER_UPDATE", "hd_plt_user", 9L, "SUCCESS", null, null, 1,
        Map.of(
            "displayName", "旧名称",
            "profile", Map.of("ownerName", "旧负责人"),
            "passwordHash", "never-return"),
        Map.of(
            "displayName", "新名称",
            "profile", Map.of("ownerName", "新负责人"),
            "passwordHash", "never-return"),
        new AuditContext(
            null, "admin@example.com", "管理员", "req-round-trip", "127.0.0.1",
            "PATCH", "/api/v1/platform/users/9")));

    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "ACCOUNT")
            .param("subjectType", "USER")
            .param("subjectId", "9")
            .with(user("admin@example.com").authorities(
                authority("audit.read"), authority("account.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].beforeData.displayName").value("旧名称"))
        .andExpect(jsonPath("$.data[0].afterData.displayName").value("新名称"))
        .andExpect(jsonPath("$.data[0].beforeData.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.data[0].afterData.passwordHash").doesNotExist())
        .andExpect(jsonPath(
            "$.data[0].changes[?(@.fieldName == 'profile.ownerName')].fieldLabel")
            .value("责任人"));
  }

  @Test
  void unboundStudyScopedFailuresAreVisibleOnlyToTheirOperator() {
    auditLogs.insert(new AuditEvent(
        "RISK", "RISK", null, null, null, null, null, null,
        "RISK_CREATE", "hd_plt_risk", null, "DENIED", "out of scope", "RISK_FORBIDDEN", 1,
        null, null, new AuditContext(
            101L, "one@example.com", "One", "req-one", "127.0.0.1",
            "POST", "/api/v1/risk-management/risks")));
    auditLogs.insert(new AuditEvent(
        "RISK", "RISK", null, null, null, null, null, null,
        "RISK_CREATE", "hd_plt_risk", null, "DENIED", "out of scope", "RISK_FORBIDDEN", 1,
        null, null, new AuditContext(
            102L, "two@example.com", "Two", "req-two", "127.0.0.1",
            "POST", "/api/v1/risk-management/risks")));

    var page = auditLogs.findPage(new AuditLogRepository.AuditQuery(
        "RISK", null, null, null, null, null, null,
        "DENIED", 1, 20, true, 101L));

    org.assertj.core.api.Assertions.assertThat(page.data())
        .extracting(AuditLogRepository.AuditLogRecord::requestId)
        .containsExactly("req-one");
  }

  @Test
  void listsRecordAuditLogsWithSanitizedSnapshotsAndChanges() throws Exception {
    jdbc.update("""
        INSERT INTO hd_plt_audit_log(
          operator_email, operator_display_name, action_code, target_table, target_id,
          module_code, subject_type, subject_id, subject_code, result_code,
          request_id, ip_address, request_method, request_path, payload_version,
          before_data, after_data)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
          JSON_OBJECT('displayName', '旧名称'), JSON_OBJECT('displayName', '新名称'))
        """,
        "admin@example.com", "管理员", "USER_UPDATE", "hd_plt_user_account", 7L,
        "ACCOUNT", "USER", 7L, "user@example.com", "SUCCESS",
        "req-test-1", "127.0.0.1", "PUT", "/api/v1/platform/users/7", 1);

    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "ACCOUNT")
            .param("subjectType", "USER")
            .param("subjectId", "7")
            .param("page", "1")
            .param("pageSize", "20")
            .with(user("admin@example.com").authorities(
                authority("audit.read"), authority("account.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].requestId").value("req-test-1"))
        .andExpect(jsonPath("$.data[0].beforeData.displayName").value("旧名称"))
        .andExpect(jsonPath("$.data[0].afterData.displayName").value("新名称"))
        .andExpect(jsonPath("$.data[0].changes[0].fieldName").value("displayName"))
        .andExpect(jsonPath("$.data[0].changes[0].beforeValue").value("旧名称"))
        .andExpect(jsonPath("$.data[0].changes[0].afterValue").value("新名称"))
        .andExpect(jsonPath("$.totalItems").value(1));
  }

  @Test
  void filtersMilestoneLogsByStudyAndStageGroup() throws Exception {
    insertGroupedAuditLog(
        "req-stage-a", "MILESTONE", "MILESTONE_STAGE", null, "PHASE_1", 501L);
    insertGroupedAuditLog(
        "req-other-study", "MILESTONE", "MILESTONE_STAGE", null, "PHASE_1", 502L);
    insertGroupedAuditLog(
        "req-other-stage", "MILESTONE", "MILESTONE_STAGE", null, "PHASE_2", 501L);

    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "MILESTONE")
            .param("scopeStudyId", "501")
            .param("groupType", "MILESTONE_STAGE")
            .param("groupCode", "PHASE_1")
            .with(user("admin@example.com").authorities(
                authority("audit.read"), authority("milestone.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItems").value(1))
        .andExpect(jsonPath("$.data[0].requestId").value("req-stage-a"));
  }

  @Test
  void filtersMonthlyLogsByFunctionReportGroup() throws Exception {
    insertGroupedAuditLog(
        "req-monthly-a", "MONTHLY", "MONTHLY_FUNCTION", 8001L, "PM", 501L);
    insertGroupedAuditLog(
        "req-other-report", "MONTHLY", "MONTHLY_FUNCTION", 8002L, "PM", 501L);

    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "MONTHLY")
            .param("groupType", "MONTHLY_FUNCTION")
            .param("groupId", "8001")
            .with(user("admin@example.com").authorities(
                authority("audit.read"), authority("monthly.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItems").value(1))
        .andExpect(jsonPath("$.data[0].requestId").value("req-monthly-a"));
  }

  @Test
  void toleratesLegacyArraySnapshotsFromTeamAssignments() throws Exception {
    // 旧团队矩阵审计曾把成员用户 ID 列表直接存成 JSON 数组（List.toString() 恰好是合法 JSON）
    jdbc.update("""
        INSERT INTO hd_plt_audit_log(
          operator_email, action_code, target_table, target_id,
          module_code, subject_type, subject_id, result_code,
          request_id, payload_version, before_data, after_data)
        VALUES (?, ?, ?, ?, ?, ?, ?, 'SUCCESS', ?, 1, '[1, 2]', '[1, 2, 3]')
        """,
        "admin@example.com", "TEAM_ASSIGNMENT_REPLACE", "hd_plt_team_assignment", 7L,
        "TEAM", "STUDY", 7L, "req-legacy-array");

    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "TEAM")
            .param("subjectType", "STUDY")
            .param("subjectId", "7")
            .with(user("admin@example.com").authorities(
                authority("audit.read"), authority("team.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItems").value(1))
        .andExpect(jsonPath("$.data[0].requestId").value("req-legacy-array"))
        .andExpect(jsonPath("$.data[0].beforeData.memberUserIds[0]").value(1))
        .andExpect(jsonPath("$.data[0].afterData.memberUserIds[2]").value(3))
        .andExpect(jsonPath("$.data[0].changes[0].fieldName").value("memberUserIds"))
        .andExpect(jsonPath("$.data[0].changes[0].fieldLabel").value("成员用户 ID"));
  }

  @Test
  void requiresAuditReadAndOriginalModuleReadPermission() throws Exception {
    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "ACCOUNT")
            .with(user("account-reader").authorities(authority("account.page.view"))))
        .andExpect(status().isForbidden());

    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "ACCOUNT")
            .with(user("audit-reader").authorities(authority("audit.read"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsAnExplicitStudyOutsideAssignedStudyScope() throws Exception {
    long assignedStudyId = seedStudy("AUDIT-SCOPE-A", "AUDIT-SCOPE-A");
    long forbiddenStudyId = seedStudy("AUDIT-SCOPE-B", "AUDIT-SCOPE-B");
    String email = "audit-scope-" + UUID.randomUUID() + "@example.com";

    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', 'Audit scoped user', 'ACTIVE', ?, 'seed', 'seed')
        """, email, UUID.randomUUID().toString());
    long userId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code,
            is_system_role, sys_create_by, sys_update_by)
        VALUES (?, 'Audit scope test', 'ASSIGNED_STUDY', 'ACTIVE', 0, 'seed', 'seed')
        """, "AUDIT_SCOPE_" + UUID.randomUUID());
    long roleId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_role WHERE role_description = 'Audit scope test'",
        Long.class);
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        VALUES (?, ?, 'seed', 'seed')
        """, userId, roleId);
    jdbc.update("""
        INSERT INTO hd_plt_team_assignment(
            study_id, team_role_id, user_id, function_line_id,
            team_role_code_snapshot, team_role_name_snapshot,
            function_line_code_snapshot, function_line_name_snapshot,
            user_email_snapshot, user_name_snapshot, sys_create_by, sys_update_by)
        SELECT ?, tr.id, ?, tr.function_line_id, tr.role_code, tr.role_name,
            fl.function_code, fl.function_name, ?, 'Audit scoped user', 'seed', 'seed'
        FROM hd_plt_team_role tr
        LEFT JOIN hd_plt_function_line fl ON fl.id = tr.function_line_id
        ORDER BY tr.id
        LIMIT 1
        """, assignedStudyId, userId, email);

    mvc.perform(get("/api/v1/audit-logs")
            .param("moduleCode", "MILESTONE")
            .param("scopeStudyId", String.valueOf(forbiddenStudyId))
            .with(user(email).authorities(
                authority("audit.read"), authority("milestone.read"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("AUDIT_FORBIDDEN"));
  }

  @Test
  void v22BackfillsRecognizableMilestoneAndMonthlyGroups() {
    String databaseName = "audit_backfill_" + UUID.randomUUID().toString().replace("-", "");
    String url = "jdbc:h2:mem:" + databaseName + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    var dataSource = new DriverManagerDataSource(url, "sa", "");
    var isolatedJdbc = new JdbcTemplate(dataSource);
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration/h2")
        .target(MigrationVersion.fromVersion("21"))
        .load()
        .migrate();

    isolatedJdbc.update("""
        INSERT INTO hd_plt_study_milestone(
            study_id, stage_code, milestone_code, sys_create_by, sys_update_by)
        VALUES (701, 'PHASE_2', 'FIRST_PATIENT_IN', 'seed', 'seed')
        """);
    long milestoneId = isolatedJdbc.queryForObject(
        "SELECT id FROM hd_plt_study_milestone WHERE milestone_code = 'FIRST_PATIENT_IN'",
        Long.class);
    isolatedJdbc.update("""
        INSERT INTO hd_plt_monthly_report(
            study_id, report_month, function_line_id,
            function_line_code_snapshot, function_line_name_snapshot,
            study_code_snapshot, program_code_snapshot, product_name_snapshot,
            project_code_snapshot, therapeutic_area_code_snapshot,
            therapeutic_area_name_snapshot, indication_description_snapshot,
            sys_create_by, sys_update_by)
        VALUES (
            702, DATE '2026-07-01', 88, 'CM', '临床运营',
            'STUDY-702', 'PROGRAM-702', 'PRODUCT-702',
            'PROJECT-702', 'TA-702', '肿瘤', '实体瘤', 'seed', 'seed')
        """);
    long reportId = isolatedJdbc.queryForObject(
        "SELECT id FROM hd_plt_monthly_report WHERE study_id = 702", Long.class);
    isolatedJdbc.update("""
        INSERT INTO hd_plt_monthly_report_entry(
            monthly_report_id, entry_date, sequence_no, progress_content,
            sys_create_by, sys_update_by)
        VALUES (?, DATE '2026-07-15', 1, '进展', 'seed', 'seed')
        """, reportId);
    long entryId = isolatedJdbc.queryForObject(
        "SELECT id FROM hd_plt_monthly_report_entry WHERE monthly_report_id = ?",
        Long.class, reportId);
    isolatedJdbc.update("""
        INSERT INTO hd_plt_audit_log(
            operator_email, action_code, target_table, target_id,
            module_code, result_code, request_id, payload_version)
        VALUES ('admin@example.com', 'MILESTONE_UPDATE', 'hd_plt_study_milestone', ?,
            'MILESTONE', 'SUCCESS', 'backfill-milestone', 1)
        """, milestoneId);
    isolatedJdbc.update("""
        INSERT INTO hd_plt_audit_log(
            operator_email, action_code, target_table, target_id,
            module_code, result_code, request_id, payload_version)
        VALUES ('admin@example.com', 'MONTHLY_SAVE', 'hd_plt_monthly_report_entry', ?,
            'MONTHLY', 'SUCCESS', 'backfill-monthly', 1)
        """, entryId);

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration/h2")
        .load()
        .migrate();

    org.assertj.core.api.Assertions.assertThat(isolatedJdbc.queryForMap("""
        SELECT group_type, group_code, scope_study_id
        FROM hd_plt_audit_log WHERE request_id = 'backfill-milestone'
        """))
        .containsEntry("GROUP_TYPE", "MILESTONE_STAGE")
        .containsEntry("GROUP_CODE", "PHASE_2")
        .containsEntry("SCOPE_STUDY_ID", 701L);
    org.assertj.core.api.Assertions.assertThat(isolatedJdbc.queryForMap("""
        SELECT group_type, group_id, group_code, scope_study_id
        FROM hd_plt_audit_log WHERE request_id = 'backfill-monthly'
        """))
        .containsEntry("GROUP_TYPE", "MONTHLY_FUNCTION")
        .containsEntry("GROUP_ID", reportId)
        .containsEntry("GROUP_CODE", "CM")
        .containsEntry("SCOPE_STUDY_ID", 702L);
  }

  @Test
  void migrationSeedsAuditReadForAdminOnly() {
    Integer adminCount = jdbc.queryForObject("""
        SELECT COUNT(*)
        FROM hd_plt_role_permission rp
        JOIN hd_plt_role r ON r.id = rp.role_id
        JOIN hd_plt_permission p ON p.id = rp.permission_id
        WHERE r.role_name = 'ADMIN' AND p.permission_code = 'audit.read'
        """, Integer.class);
    Integer otherCount = jdbc.queryForObject("""
        SELECT COUNT(*)
        FROM hd_plt_role_permission rp
        JOIN hd_plt_role r ON r.id = rp.role_id
        JOIN hd_plt_permission p ON p.id = rp.permission_id
        WHERE r.role_name <> 'ADMIN' AND p.permission_code = 'audit.read'
        """, Integer.class);

    org.assertj.core.api.Assertions.assertThat(adminCount).isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(otherCount).isZero();
  }

  private void insertGroupedAuditLog(
      String requestId,
      String moduleCode,
      String groupType,
      Long groupId,
      String groupCode,
      Long scopeStudyId) {
    jdbc.update("""
        INSERT INTO hd_plt_audit_log(
          operator_email, action_code, target_table, module_code, subject_type,
          scope_study_id, group_type, group_id, group_code, result_code, request_id,
          payload_version)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'SUCCESS', ?, 1)
        """,
        "admin@example.com", moduleCode + "_UPDATE", "hd_plt_test",
        moduleCode, "MILESTONE", scopeStudyId, groupType, groupId, groupCode, requestId);
  }

  private long seedStudy(String studyCode, String suffix) {
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(
            area_code, area_name, status_code, sys_create_by, sys_update_by)
        VALUES (?, '肿瘤', 'ACTIVE', 'seed', 'seed')
        """, "TA-" + suffix);
    jdbc.update("""
        INSERT INTO hd_plt_program(
            program_code, product_name, status_code, sys_create_by, sys_update_by)
        VALUES (?, ?, 'ACTIVE', 'seed', 'seed')
        """, "PROGRAM-" + suffix, "HD-" + suffix);
    jdbc.update("""
        INSERT INTO hd_plt_project(
            project_code, program_id, indication_description, therapeutic_area_id,
            sys_create_by, sys_update_by)
        SELECT ?, p.id, '实体瘤', ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = ? AND ta.area_code = ?
        """, "PROJECT-" + suffix, "PROGRAM-" + suffix, "TA-" + suffix);
    jdbc.update("""
        INSERT INTO hd_plt_study(
            study_code, phase_status_code, program_id, program_code_snapshot,
            product_name_snapshot, project_id, project_code_snapshot,
            therapeutic_area_id, therapeutic_area_code_snapshot,
            therapeutic_area_name_snapshot, indication_description_snapshot,
            sys_create_by, sys_update_by)
        SELECT ?, 'PHASE_1', p.id, p.program_code, p.product_name,
            pr.id, pr.project_code, ta.id, ta.area_code, ta.area_name,
            pr.indication_description, 'seed', 'seed'
        FROM hd_plt_program p JOIN hd_plt_project pr ON pr.program_id = p.id
        JOIN hd_plt_therapeutic_area ta ON ta.id = pr.therapeutic_area_id
        WHERE p.program_code = ? AND pr.project_code = ?
        """, studyCode, "PROGRAM-" + suffix, "PROJECT-" + suffix);
    return jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = ?", Long.class, studyCode);
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
