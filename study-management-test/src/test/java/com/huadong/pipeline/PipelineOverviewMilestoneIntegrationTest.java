package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** End-to-end check that the pipeline overview derives each phase's status from milestones. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PipelineOverviewMilestoneIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired UserDetailsService userDetailsService;
  @Autowired JdbcTemplate jdbc;

  @Test
  void overviewStatusReflectsMilestoneSubStatus() throws Exception {
    seedHierarchy();
    createStudy("HD-MS-001", "PHASE_1");
    long studyId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code='HD-MS-001'", Long.class);

    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    for (int i = 0; i <= 5; i++) insertMilestone(studyId, "PreIND", "PreIND-" + i, s, e);
    for (int i = 0; i <= 4; i++) insertMilestone(studyId, "IND", "IND-" + i, s, e);

    mvc.perform(get("/api/v1/clinical-pipeline/overview")
            .with(user(userDetailsService.loadUserByUsername("admin@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].statusLabel").value("进行中"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].mainStageLabel").value("IND"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].subStatusLabel").value("IND 获批"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].globallyCompleted").value(false))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].currentPhaseCompleted").value(true));
  }

  @Test
  void overviewFallsBackToDateBasedWhenNoMilestones() throws Exception {
    seedHierarchy();
    createStudy("HD-MS-002", "PHASE_1");

    mvc.perform(get("/api/v1/clinical-pipeline/overview")
            .with(user(userDetailsService.loadUserByUsername("admin@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].statusLabel").value("计划中"));
  }

  @Test
  void overviewFallsBackToBaseStatusWithoutMilestoneReadPermission() throws Exception {
    seedHierarchy();
    createStudy("HD-MS-003", "PHASE_1");
    long studyId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code='HD-MS-003'", Long.class);

    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    for (int i = 0; i <= 5; i++) insertMilestone(studyId, "PreIND", "PreIND-" + i, s, e);
    for (int i = 0; i <= 4; i++) insertMilestone(studyId, "IND", "IND-" + i, s, e);

    String viewer = "pipeline.viewer@example.com";
    seedUserWithPermission(viewer, "pipeline.page.view");

    mvc.perform(get("/api/v1/clinical-pipeline/overview")
            .with(user(viewer).authorities(authority("pipeline.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].statusLabel").value("计划中"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].mainStageLabel").value(""))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].subStatusLabel").value(""));
  }

  private void seedUserWithPermission(String email, String permissionCode) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', ?, 'seed', 'seed')
        """, email, email, UUID.randomUUID().toString());
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
  }

  private void createStudy(String code, String phase) throws Exception {
    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(user("researcher").authorities(new SimpleGrantedAuthority("config.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"" + code + "\",\"programCode\":\"PROGRAM-001\","
                + "\"projectCode\":\"PROJECT-001\",\"therapeuticAreaCode\":\"ONCOLOGY\","
                + "\"phase\":\"" + phase + "\"}"))
        .andExpect(status().isCreated());
  }

  private void insertMilestone(long studyId, String stage, String code, LocalDate start, LocalDate end) {
    jdbc.update("INSERT INTO hd_plt_study_milestone(study_id, stage_code, milestone_code, "
        + "actual_start_date, actual_end_date, sys_create_by, sys_update_by) "
        + "VALUES (?,?,?,?,?,'seed','seed')", studyId, stage, code, start, end);
  }

  private void seedHierarchy() {
    jdbc.update("DELETE FROM hd_plt_study_milestone");
    jdbc.update("DELETE FROM hd_plt_study");
    jdbc.update("DELETE FROM hd_plt_project");
    jdbc.update("DELETE FROM hd_plt_program");
    jdbc.update("DELETE FROM hd_plt_therapeutic_area");
    jdbc.update("INSERT INTO hd_plt_therapeutic_area(area_code, area_name, status_code, "
        + "sys_create_by, sys_update_by) VALUES ('ONCOLOGY','肿瘤','ACTIVE','seed','seed')");
    jdbc.update("INSERT INTO hd_plt_program(program_code, product_name, status_code, "
        + "sys_create_by, sys_update_by) VALUES ('PROGRAM-001','HD-001','ACTIVE','seed','seed')");
    jdbc.update("INSERT INTO hd_plt_project(project_code, program_id, indication_description, "
        + "therapeutic_area_id, sys_create_by, sys_update_by) "
        + "SELECT 'PROJECT-001', p.id, '实体瘤', ta.id, 'seed', 'seed' "
        + "FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta "
        + "WHERE p.program_code='PROGRAM-001' AND ta.area_code='ONCOLOGY'");
  }

  private void seedUser(String email) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', ?, 'seed', 'seed')
        """, email, email, UUID.randomUUID().toString());
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
