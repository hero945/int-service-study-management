package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end check that the pipeline overview derives each phase's status from milestones. */
@SpringBootTest
@AutoConfigureMockMvc
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
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].statusLabel").value("已完成"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].mainStageLabel").value("IND"))
        .andExpect(jsonPath("$.areas[0].projects[0].studies[0].subStatusLabel").value("IND 获批"))
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
}
