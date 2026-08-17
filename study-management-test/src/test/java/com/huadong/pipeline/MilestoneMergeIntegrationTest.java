package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * End-to-end checks that the study milestone page combines Project milestone data
 * for the 5 regulatory stages with Study milestone data for the remaining stages.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MilestoneMergeIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void getMilestonesCombinesProjectRegulatoryStagesAndStudyNonRegulatoryStages() throws Exception {
    long studyId = seedStudy("MERGE-001");

    // Update a project milestone in the PreIND regulatory stage
    mvc.perform(put("/api/v1/studies/{id}/project-milestones/{code}", studyId, "PreIND-0")
            .with(user("admin@example.com").authorities(authority("project.milestone.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"planV1Date":"2026-01-15","planV2Date":null,
                 "actualStartDate":"2026-01-20","actualEndDate":"2026-01-25",
                 "deviationNote":"project data"}
                """))
        .andExpect(status().isOk());

    // Update a study milestone in the Protocol non-regulatory stage
    mvc.perform(put("/api/v1/studies/{id}/milestones/{code}", studyId, "Protocol-0")
            .with(user("admin@example.com").authorities(authority("milestone.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"planV1Date":"2026-02-01","planV2Date":null,
                 "actualStartDate":"2026-02-05","actualEndDate":"2026-02-10",
                 "deviationNote":"study data"}
                """))
        .andExpect(status().isOk());

    // Study milestone page should reflect project data for PreIND and study data for Protocol
    mvc.perform(get("/api/v1/studies/{id}/milestones", studyId)
            .with(user("admin@example.com").authorities(authority("milestone.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groups.length()").value(10))
        // PreIND is a project-source regulatory stage
        .andExpect(jsonPath("$.groups[0].stageCode").value("PreIND"))
        .andExpect(jsonPath("$.groups[0].nodes[0].milestoneCode").value("PreIND-0"))
        .andExpect(jsonPath("$.groups[0].nodes[0].planV1Date").value("2026-01-15"))
        .andExpect(jsonPath("$.groups[0].nodes[0].actualEndDate").value("2026-01-25"))
        .andExpect(jsonPath("$.groups[0].nodes[0].deviationNote").value("project data"))
        .andExpect(jsonPath("$.groups[0].nodes[0].source").value("PROJECT"))
        // Protocol is a study-source non-regulatory stage
        .andExpect(jsonPath("$.groups[3].stageCode").value("Protocol"))
        .andExpect(jsonPath("$.groups[3].nodes[0].milestoneCode").value("Protocol-0"))
        .andExpect(jsonPath("$.groups[3].nodes[0].planV1Date").value("2026-02-01"))
        .andExpect(jsonPath("$.groups[3].nodes[0].actualEndDate").value("2026-02-10"))
        .andExpect(jsonPath("$.groups[3].nodes[0].deviationNote").value("study data"))
        .andExpect(jsonPath("$.groups[3].nodes[0].source").value("STUDY"));
  }

  @Test
  void getStageProjectionReflectsMergedProjectMilestoneData() throws Exception {
    long studyId = seedStudy("MERGE-PROJ-001");

    // Finish PreIND-0 via project milestone endpoint
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

    // Stage projection uses frontier: PreIND-0 is the last node with actual dates
    mvc.perform(get("/api/v1/studies/{id}/stage-projection", studyId)
            .with(user("admin@example.com").authorities(authority("milestone.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentStageCode").value("PreIND"))
        .andExpect(jsonPath("$.currentMilestoneCode").value("PreIND-0"))
        .andExpect(jsonPath("$.statusText").value("进行中"));
  }

  private long seedStudy(String studyCode) {
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(
            area_code, area_name, status_code, sys_create_by, sys_update_by)
        SELECT ?, '肿瘤', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = ?)
        """, "TA-MERGE", "TA-MERGE");
    jdbc.update("""
        INSERT INTO hd_plt_program(
            program_code, product_name, status_code, sys_create_by, sys_update_by)
        SELECT ?, 'MERGE-001', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_program WHERE program_code = ?)
        """, "PROGRAM-MERGE-001", "PROGRAM-MERGE-001");
    jdbc.update("""
        INSERT INTO hd_plt_project(
            project_code, program_id, indication_description,
            therapeutic_area_id, sys_create_by, sys_update_by)
        SELECT 'PROJECT-MERGE-001', p.id, '实体瘤', ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = 'PROGRAM-MERGE-001' AND ta.area_code = 'TA-MERGE'
        AND NOT EXISTS (SELECT 1 FROM hd_plt_project WHERE project_code = 'PROJECT-MERGE-001')
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
        WHERE p.program_code = 'PROGRAM-MERGE-001' AND pr.project_code = 'PROJECT-MERGE-001'
        """, studyCode);
    return jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = ?", Long.class, studyCode);
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
