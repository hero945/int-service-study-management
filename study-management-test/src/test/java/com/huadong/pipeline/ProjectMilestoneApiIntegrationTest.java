package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

/**
 * End-to-end checks for project-level regulatory milestones accessed through
 * the study-scoped project milestone endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectMilestoneApiIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void getProjectMilestonesReturnsRegulatoryGroupsForStudyProject() throws Exception {
    long studyId = seedStudy("PM-GET-001");

    mvc.perform(get("/api/v1/studies/{id}/project-milestones", studyId)
            .with(user("admin@example.com").authorities(authority("project.milestone.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.projectCode").value("PROJECT-PM-001"))
        .andExpect(jsonPath("$.groups.length()").value(10))
        .andExpect(jsonPath("$.groups[0].stageCode").value("PreIND"))
        .andExpect(jsonPath("$.groups[0].nodes.length()").value(6))
        .andExpect(jsonPath("$.groups[0].nodes[0].milestoneCode").value("PreIND-0"))
        .andExpect(jsonPath("$.groups[0].nodes[0].status").exists());
  }

  @Test
  void updateProjectMilestonePersistsDatesAndReturnsUpdatedPage() throws Exception {
    long studyId = seedStudy("PM-PUT-001");

    mvc.perform(put("/api/v1/studies/{id}/project-milestones/{code}", studyId, "PreIND-0")
            .with(user("admin@example.com").authorities(authority("project.milestone.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"planV1Date":"2026-01-15","planV2Date":null,
                 "actualStartDate":"2026-01-20","actualEndDate":"2026-01-25",
                 "deviationNote":"提前完成"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.projectCode").value("PROJECT-PM-001"))
        .andExpect(jsonPath("$.groups[0].nodes[0].planV1Date").value("2026-01-15"))
        .andExpect(jsonPath("$.groups[0].nodes[0].actualStartDate").value("2026-01-20"))
        .andExpect(jsonPath("$.groups[0].nodes[0].actualEndDate").value("2026-01-25"))
        .andExpect(jsonPath("$.groups[0].nodes[0].deviationNote").value("提前完成"));

    long projectId = jdbc.queryForObject(
        "SELECT project_id FROM hd_plt_study WHERE id = ?", Long.class, studyId);
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_project_milestone WHERE project_id = ? AND milestone_code = ?",
        Integer.class, projectId, "PreIND-0");
    org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
  }

  @Test
  void getProjectStageProjectionReflectsFirstUnfinishedNode() throws Exception {
    long studyId = seedStudy("PM-PROJ-001");

    // Finish PreIND-0, current node should become PreIND-1
    mvc.perform(put("/api/v1/studies/{id}/project-milestones/{code}", studyId, "PreIND-0")
            .with(user("admin@example.com").authorities(authority("project.milestone.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"planV1Date":null,"planV2Date":null,
                 "actualStartDate":"2026-01-01","actualEndDate":"2026-01-10",
                 "deviationNote":null}
                """))
        .andExpect(status().isOk());

    mvc.perform(get("/api/v1/studies/{id}/project-milestones/stage-projection", studyId)
            .with(user("admin@example.com").authorities(authority("project.milestone.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentStageCode").value("PreIND"))
        .andExpect(jsonPath("$.currentMilestoneCode").value("PreIND-1"))
        .andExpect(jsonPath("$.statusText").value("进行中"));
  }

  @Test
  void getProjectMilestonesWithoutReadPermissionIsForbidden() throws Exception {
    long studyId = seedStudy("PM-NO-READ-001");

    mvc.perform(get("/api/v1/studies/{id}/project-milestones", studyId)
            .with(user("no.project.read@example.com").authorities(authority("milestone.read"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateProjectMilestoneWithoutUpdatePermissionIsForbidden() throws Exception {
    long studyId = seedStudy("PM-NO-UPDATE-001");

    mvc.perform(put("/api/v1/studies/{id}/project-milestones/{code}", studyId, "PreIND-0")
            .with(user("no.project.update@example.com").authorities(authority("project.milestone.read")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"planV1Date":"2026-01-15","planV2Date":null,
                 "actualStartDate":null,"actualEndDate":null,"deviationNote":null}
                """))
        .andExpect(status().isForbidden());
  }

  private long seedStudy(String studyCode) {
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(
            area_code, area_name, status_code, sys_create_by, sys_update_by)
        SELECT ?, '肿瘤', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = ?)
        """, "TA-PM", "TA-PM");
    jdbc.update("""
        INSERT INTO hd_plt_program(
            program_code, product_name, status_code, sys_create_by, sys_update_by)
        SELECT ?, 'PM-001', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_program WHERE program_code = ?)
        """, "PROGRAM-PM-001", "PROGRAM-PM-001");
    jdbc.update("""
        INSERT INTO hd_plt_project(
            project_code, program_id, indication_description,
            therapeutic_area_id, sys_create_by, sys_update_by)
        SELECT 'PROJECT-PM-001', p.id, '实体瘤', ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = 'PROGRAM-PM-001' AND ta.area_code = 'TA-PM'
        AND NOT EXISTS (SELECT 1 FROM hd_plt_project WHERE project_code = 'PROJECT-PM-001')
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
        WHERE p.program_code = 'PROGRAM-PM-001' AND pr.project_code = 'PROJECT-PM-001'
        """, studyCode);
    return jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = ?", Long.class, studyCode);
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
