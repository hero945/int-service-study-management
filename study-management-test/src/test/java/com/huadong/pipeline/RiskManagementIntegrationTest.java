package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class RiskManagementIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void formOptionsExposeActiveScoringRuleFromDatabase() throws Exception {
    String operator = "risk.rule.reader@example.com";
    seedUser(operator, "规则读取人");
    mvc.perform(get("/api/v1/risk-management/form-options")
            .with(user(operator).authorities(authority("risk.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scoringRule.lowMax").value(12))
        .andExpect(jsonPath("$.scoringRule.mediumMax").value(36))
        .andExpect(jsonPath("$.scoringRule.id").isNumber());
  }

  @Test
  void createsAndListsRiskWithAssessmentAndControlAction() throws Exception {
    String operator = "risk.owner@example.com";
    long ownerId = seedUser(operator, "风险负责人");
    long studyId = seedStudy("RISK-STUDY-001");
    assignToStudy(studyId, ownerId);
    long functionLineId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_function_line WHERE function_code = 'PM'", Long.class);

    String response = mvc.perform(post("/api/v1/risk-management/risks")
            .with(user(operator).authorities(authority("risk.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "studyId":%d,
                  "functionLineId":%d,
                  "ownerUserId":%d,
                  "description":"首家中心启动可能晚于计划",
                  "registeredDate":"2026-07-22",
                  "assessment":{"impact":4,"likelihood":4,"detectability":3,
                    "reason":"首次评估"},
                  "actions":[{"description":"每周跟踪中心启动材料",
                    "ownerUserId":%d,"plannedDate":"2026-08-15"}]
                }
                """.formatted(studyId, functionLineId, ownerId, ownerId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.risk.riskCode").value(org.hamcrest.Matchers.matchesPattern(
            "RSK-2026-[0-9]{6}")))
        .andExpect(jsonPath("$.risk.score").value(48))
        .andExpect(jsonPath("$.risk.level").value("HIGH"))
        .andExpect(jsonPath("$.actions.length()").value(1))
        .andExpect(jsonPath("$.assessments.length()").value(1))
        .andReturn().getResponse().getContentAsString();

    String riskCode = new com.fasterxml.jackson.databind.ObjectMapper()
        .readTree(response).get("risk").get("riskCode").asText();

    String withSecondAction = mvc.perform(post(
            "/api/v1/risk-management/risks/{riskCode}/actions", riskCode)
            .with(user(operator).authorities(authority("risk.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"expectedRiskVersion":0,"action":{"description":"准备备选中心",
                 "ownerUserId":%d,"plannedDate":"2026-08-20","status":"OPEN"}}
                """.formatted(ownerId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.actions.length()").value(2))
        .andReturn().getResponse().getContentAsString();
    long actionId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(withSecondAction)
        .get("actions").get(1).get("id").asLong();

    mvc.perform(patch("/api/v1/risk-management/risks/{riskCode}/actions/{actionId}",
            riskCode, actionId)
            .with(user(operator).authorities(authority("risk.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"expectedVersion":0,"action":{"description":"准备备选中心",
                 "ownerUserId":%d,"plannedDate":"2026-08-20",
                 "completedDate":"2026-08-10","status":"COMPLETED",
                 "completionNote":"备选中心已确认"}}
                """.formatted(ownerId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.actions[1].status").value("COMPLETED"))
        .andExpect(jsonPath("$.actions[1].version").value(1));

    mvc.perform(delete("/api/v1/risk-management/risks/{riskCode}/actions/{actionId}",
            riskCode, actionId)
            .param("expectedVersion", "1")
            .with(user(operator).authorities(authority("risk.update")))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.actions.length()").value(1));

    mvc.perform(get("/api/v1/risk-management/risks")
            .param("query", riskCode)
            .with(user(operator).authorities(authority("risk.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].riskCode").value(riskCode))
        .andExpect(jsonPath("$.data[0].actionCount").value(1))
        .andExpect(jsonPath("$.stats.total").value(1))
        .andExpect(jsonPath("$.stats.open").value(1))
        .andExpect(jsonPath("$.stats.high").value(1));
  }

  @Test
  void requiresPermissionAndCsrfForCreation() throws Exception {
    mvc.perform(get("/api/v1/risk-management/risks")
            .with(user("viewer@example.com")))
        .andExpect(status().isForbidden());

    mvc.perform(post("/api/v1/risk-management/risks")
            .with(user("writer@example.com").authorities(authority("risk.create")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void filtersRisksByStudyId() throws Exception {
    String operator = "risk.scoped@example.com";
    long ownerId = seedUser(operator, "风险范围用户");
    long studyA = seedStudy("RISK-STUDY-A");
    long studyB = seedStudyWith("RISK-STUDY-B", "B");
    assignToStudy(studyA, ownerId);
    assignToStudy(studyB, ownerId);
    long functionLineId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_function_line WHERE function_code = 'PM'", Long.class);

    String created = mvc.perform(post("/api/v1/risk-management/risks")
            .with(user(operator).authorities(authority("risk.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"studyId":%d,"functionLineId":%d,"ownerUserId":%d,
                 "description":"仅属于A研究的risk","registeredDate":"2026-07-22",
                 "assessment":{"impact":3,"likelihood":2,"detectability":2,
                   "reason":"初始评估"},"actions":[]}
                """.formatted(studyA, functionLineId, ownerId)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    String riskCode = new com.fasterxml.jackson.databind.ObjectMapper()
        .readTree(created).get("risk").get("riskCode").asText();

    mvc.perform(get("/api/v1/risk-management/risks")
            .param("studyId", String.valueOf(studyA))
            .with(user(operator).authorities(authority("risk.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].riskCode").value(riskCode));

    mvc.perform(get("/api/v1/risk-management/risks")
            .param("studyId", String.valueOf(studyB))
            .with(user(operator).authorities(authority("risk.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void closesReopensAndRejectsAStaleVersion() throws Exception {
    String operator = "risk.editor@example.com";
    long ownerId = seedUser(operator, "风险编辑人");
    long studyId = seedStudy("RISK-STUDY-002");
    assignToStudy(studyId, ownerId);
    long functionLineId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_function_line WHERE function_code = 'PM'", Long.class);

    String created = mvc.perform(post("/api/v1/risk-management/risks")
            .with(user(operator).authorities(authority("risk.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"studyId":%d,"functionLineId":%d,"ownerUserId":%d,
                 "description":"研究启动进度风险","registeredDate":"2026-07-22",
                 "assessment":{"impact":3,"likelihood":2,"detectability":2,
                   "reason":"初始评估"},"actions":[]}
                """.formatted(studyId, functionLineId, ownerId)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    String riskCode = new com.fasterxml.jackson.databind.ObjectMapper()
        .readTree(created).get("risk").get("riskCode").asText();

    mvc.perform(patch("/api/v1/risk-management/risks/{riskCode}", riskCode)
            .with(user(operator).authorities(authority("risk.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody(0, studyId, functionLineId, ownerId, "CLOSED", "风险已消除")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.risk.status").value("CLOSED"))
        .andExpect(jsonPath("$.risk.version").value(1))
        .andExpect(jsonPath("$.closeReason").value("风险已消除"));

    mvc.perform(patch("/api/v1/risk-management/risks/{riskCode}", riskCode)
            .with(user(operator).authorities(authority("risk.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody(1, studyId, functionLineId, ownerId, "OPEN", "出现新的延期信号")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.risk.status").value("OPEN"))
        .andExpect(jsonPath("$.risk.version").value(2))
        .andExpect(jsonPath("$.closeReason").value("风险已消除"));

    org.junit.jupiter.api.Assertions.assertEquals("出现新的延期信号", jdbc.queryForObject("""
        SELECT operation_reason FROM hd_plt_audit_log
        WHERE target_table = 'hd_plt_risk' AND action_code = 'RISK_UPDATE'
        ORDER BY id DESC LIMIT 1
        """, String.class));

    mvc.perform(patch("/api/v1/risk-management/risks/{riskCode}", riskCode)
            .with(user(operator).authorities(authority("risk.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody(0, studyId, functionLineId, ownerId, "CLOSED", "过期请求")))
        .andExpect(status().isConflict());
  }

  private static String updateBody(long version, long studyId, long functionLineId,
                                   long ownerId, String riskStatus, String reason) {
    return """
        {"expectedVersion":%d,"studyId":%d,"functionLineId":%d,"ownerUserId":%d,
         "description":"研究启动进度风险","registeredDate":"2026-07-22",
         "status":"%s","statusReason":"%s"}
        """.formatted(version, studyId, functionLineId, ownerId, riskStatus, reason);
  }

  private long seedUser(String email, String displayName) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', ?, 'seed', 'seed')
        """, email, displayName, UUID.randomUUID().toString());
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT u.id, r.id, 'seed', 'seed'
        FROM hd_plt_user u CROSS JOIN hd_plt_role r
        WHERE u.email = ? AND r.role_name = 'USER'
        """, email);
    return jdbc.queryForObject("SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);
  }

  private long seedStudy(String studyCode) {
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(
            area_code, area_name, status_code, sys_create_by, sys_update_by)
        VALUES ('TA-RISK', '肿瘤', 'ACTIVE', 'seed', 'seed')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_program(
            program_code, product_name, status_code, sys_create_by, sys_update_by)
        VALUES ('PROGRAM-RISK', 'HD-RISK', 'ACTIVE', 'seed', 'seed')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_project(
            project_code, program_id, indication_description, therapeutic_area_id,
            sys_create_by, sys_update_by)
        SELECT 'PROJECT-RISK', p.id, '实体瘤', ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = 'PROGRAM-RISK' AND ta.area_code = 'TA-RISK'
        """);
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
        WHERE p.program_code = 'PROGRAM-RISK' AND pr.project_code = 'PROJECT-RISK'
        """, studyCode);
    return jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = ?", Long.class, studyCode);
  }

  private long seedStudyWith(String studyCode, String suffix) {
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

  private void assignToStudy(long studyId, long userId) {
    jdbc.update("""
        INSERT INTO hd_plt_team_assignment(
            study_id, team_role_id, user_id, function_line_id,
            team_role_code_snapshot, team_role_name_snapshot,
            function_line_code_snapshot, function_line_name_snapshot,
            user_email_snapshot, user_name_snapshot, sys_create_by, sys_update_by)
        SELECT ?, tr.id, u.id, tr.function_line_id, tr.role_code, tr.role_name,
            fl.function_code, fl.function_name, u.email, u.display_name, 'seed', 'seed'
        FROM hd_plt_team_role tr JOIN hd_plt_function_line fl ON fl.id = tr.function_line_id
        JOIN hd_plt_user u ON u.id = ? WHERE tr.role_code = 'PL'
        """, studyId, userId);
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
