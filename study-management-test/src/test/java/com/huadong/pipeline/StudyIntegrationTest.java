package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void listReturnsTherapeuticAreaProgramAndPlPmNames() throws Exception {
    String operator = "study.owner@example.com";
    long ownerId = seedUser(operator, "孙磊");
    long studyId = seedStudy("STUDY-PLPM-001");
    assignToStudy(studyId, ownerId);

    String body = mvc.perform(get("/api/v1/clinical-pipeline/studies")
            .param("pageSize", "100")
            .with(user(operator).authorities(
                new SimpleGrantedAuthority("study.read"),
                new SimpleGrantedAuthority("milestone.read"))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    JsonNode study = null;
    for (JsonNode node : mapper.readTree(body).get("data")) {
      if ("STUDY-PLPM-001".equals(node.get("code").asText())) {
        study = node;
        break;
      }
    }
    assertTrue(study != null, "seeded study present in list");
    assertEquals("PROGRAM-RISK", study.get("programCode").asText());
    assertEquals("TA-RISK", study.get("therapeuticAreaCode").asText());
    assertEquals("肿瘤", study.get("therapeuticAreaName").asText());
    assertEquals("孙磊", study.get("plName").asText());
    assertEquals("", study.get("pmName").asText());
    assertEquals("", study.get("currentPhase").asText());
    assertEquals("", study.get("currentStatus").asText());
  }

  @Test
  void listDerivesCurrentPhaseAndStatusFromLatestIndActuals() throws Exception {
    String operator = "study.phase@example.com";
    long ownerId = seedUser(operator, "阶段测试");
    long studyId = seedStudy("STUDY-PHASE-001");
    assignToStudy(studyId, ownerId);

    // IND-0..IND-4 all have actual dates; Pre3 has plan dates only → phase stays IND.
    for (int i = 0; i <= 4; i++) {
      seedMilestone(studyId, "IND", "IND-" + i,
          null, null, "2024-01-0" + (i + 1), "2024-02-0" + (i + 1));
    }
    seedMilestone(studyId, "Pre3", "Pre3-0", "2025-01-01", "2025-02-01", null, null);

    JsonNode study = findStudyInList(operator, "STUDY-PHASE-001");
    assertEquals("IND", study.get("currentPhase").asText());
    assertEquals("IND 获批", study.get("currentStatus").asText());
  }

  @Test
  void listReturnsEmptyCurrentPhaseAndStatusWithoutActualMilestoneDates() throws Exception {
    String operator = "study.empty-phase@example.com";
    long ownerId = seedUser(operator, "空阶段测试");
    long studyId = seedStudy("STUDY-PHASE-EMPTY");
    assignToStudy(studyId, ownerId);
    seedMilestone(studyId, "IND", "IND-0", "2024-01-01", null, null, null);

    JsonNode study = findStudyInList(operator, "STUDY-PHASE-EMPTY");
    assertEquals("", study.get("currentPhase").asText());
    assertEquals("", study.get("currentStatus").asText());
  }

  @Test
  void listDoesNotDeriveMilestoneStatusWithoutMilestoneReadPermission() throws Exception {
    String operator = "study.no-milestone-read@example.com";
    long ownerId = seedUserWithPermission(operator, "无里程碑读权限", "study.read");
    long studyId = seedStudy("STUDY-NO-MS-READ");
    assignToStudy(studyId, ownerId);

    for (int i = 0; i <= 4; i++) {
      seedMilestone(studyId, "IND", "IND-" + i,
          null, null, "2024-01-0" + (i + 1), "2024-02-0" + (i + 1));
    }

    JsonNode study = findStudyInList(operator, "STUDY-NO-MS-READ");
    assertEquals("", study.get("currentPhase").asText());
    assertEquals("计划中", study.get("currentStatus").asText());
  }

  @Test
  void listSupportsServerSidePagingAndProgramFilter() throws Exception {
    String operator = "study.page@example.com";
    long ownerId = seedUser(operator, "分页测试");
    long firstId = seedStudy("STUDY-PAGE-A");
    assignToStudy(firstId, ownerId);

    String page1 = mvc.perform(get("/api/v1/clinical-pipeline/studies")
            .param("program", "PROGRAM-RISK")
            .param("page", "1")
            .param("pageSize", "1")
            .with(user(operator).authorities(new SimpleGrantedAuthority("study.read"))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    JsonNode root = mapper.readTree(page1);
    assertEquals(1, root.get("data").size());
    assertTrue(root.get("total").asInt() >= 1);
    assertEquals(1, root.get("page").asInt());
    assertEquals(1, root.get("pageSize").asInt());
    assertTrue(root.get("totalPages").asInt() >= 1);
    assertTrue(root.get("data").get(0).get("code").asText().startsWith("STUDY-"));
  }

  private long seedUserWithPermission(String email, String displayName, String permissionCode) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', 'stamp', 'seed', 'seed')
        """, email, displayName);
    long userId = jdbc.queryForObject("SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code,
            is_system_role, sys_create_by, sys_update_by)
        SELECT ?, 'Test role with single permission', 'ALL',
            'ACTIVE', 0, 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_role WHERE role_name = ?)
        """, email + "_ROLE", email + "_ROLE");
    jdbc.update("""
        INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
        SELECT r.id, p.id, 'seed', 'seed'
        FROM hd_plt_role r JOIN hd_plt_permission p ON p.permission_code = ?
        WHERE r.role_name = ?
          AND NOT EXISTS (
            SELECT 1 FROM hd_plt_role_permission rp
            WHERE rp.role_id = r.id AND rp.permission_id = p.id)
        """, permissionCode, email + "_ROLE");
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT ?, id, 'seed', 'seed'
        FROM hd_plt_role WHERE role_name = ?
        """, userId, email + "_ROLE");
    return userId;
  }

  private JsonNode findStudyInList(String operator, String studyCode) throws Exception {
    String body = mvc.perform(get("/api/v1/clinical-pipeline/studies")
            .param("pageSize", "100")
            .with(user(operator).authorities(
                new SimpleGrantedAuthority("study.read"),
                new SimpleGrantedAuthority("milestone.read"))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    for (JsonNode node : mapper.readTree(body).get("data")) {
      if (studyCode.equals(node.get("code").asText())) {
        return node;
      }
    }
    throw new AssertionError("seeded study not present in list: " + studyCode);
  }

  private void seedMilestone(
      long studyId, String stageCode, String milestoneCode,
      String planV1, String planV2, String actualStart, String actualEnd) {
    jdbc.update("""
        INSERT INTO hd_plt_study_milestone(
            study_id, stage_code, milestone_code,
            plan_v1_date, plan_v2_date, actual_start_date, actual_end_date,
            deviation_note, sys_create_by, sys_update_by)
        VALUES (?, ?, ?, ?, ?, ?, ?, NULL, 'seed', 'seed')
        """,
        studyId, stageCode, milestoneCode,
        planV1 == null ? null : java.sql.Date.valueOf(planV1),
        planV2 == null ? null : java.sql.Date.valueOf(planV2),
        actualStart == null ? null : java.sql.Date.valueOf(actualStart),
        actualEnd == null ? null : java.sql.Date.valueOf(actualEnd));
  }

  private long seedUser(String email, String displayName) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', 'stamp', 'seed', 'seed')
        """, email, displayName);
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
}
