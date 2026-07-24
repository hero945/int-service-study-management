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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MilestoneAccessIntegrationTest {

  private static final String MEMBER = "milestone.member@example.com";

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void getAndUpdateMilestonesWithinAssignedStudyScopeAreAllowed() throws Exception {
    long studyId = seedStudy("MS-SCOPE-IN");
    long memberId = seedScopedUser(MEMBER, "里程碑成员");
    assignToStudy(studyId, memberId);

    mvc.perform(get("/api/v1/studies/{id}/milestones", studyId)
            .with(user(MEMBER).authorities(authority("milestone.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studyCode").value("MS-SCOPE-IN"));

    mvc.perform(put("/api/v1/studies/{id}/milestones/{code}", studyId, "PreIND-0")
            .with(user(MEMBER).authorities(authority("milestone.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"planV1Date":"2026-01-01","planV2Date":null,
                 "actualStartDate":null,"actualEndDate":null,"deviationNote":null}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studyCode").value("MS-SCOPE-IN"));
  }

  @Test
  void getAndUpdateMilestonesOutsideAssignedStudyScopeAreForbidden() throws Exception {
    long inScopeStudy = seedStudy("MS-SCOPE-A");
    long outOfScopeStudy = seedStudyWith("MS-SCOPE-B", "B");
    long memberId = seedScopedUser(MEMBER, "里程碑成员");
    assignToStudy(inScopeStudy, memberId);

    mvc.perform(get("/api/v1/studies/{id}/milestones", outOfScopeStudy)
            .with(user(MEMBER).authorities(authority("milestone.read"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("STUDY_OUT_OF_SCOPE"));

    mvc.perform(put("/api/v1/studies/{id}/milestones/{code}", outOfScopeStudy, "PreIND-0")
            .with(user(MEMBER).authorities(authority("milestone.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"planV1Date":"2026-01-01","planV2Date":null,
                 "actualStartDate":null,"actualEndDate":null,"deviationNote":null}
                """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("STUDY_OUT_OF_SCOPE"));
  }

  @Test
  void getMilestonesWithoutReadPermissionIsForbidden() throws Exception {
    long studyId = seedStudy("MS-NO-READ");
    mvc.perform(get("/api/v1/studies/{id}/milestones", studyId)
            .with(user("no.read@example.com").authorities(authority("study.read"))))
        .andExpect(status().isForbidden());
  }

  private long seedScopedUser(String email, String displayName) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', ?, 'seed', 'seed')
        """, email, displayName, UUID.randomUUID().toString());
    long userId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code,
            is_system_role, sys_create_by, sys_update_by)
        SELECT 'MILESTONE_SCOPED', 'Assigned-study milestone role', 'ASSIGNED_STUDY',
            'ACTIVE', 0, 'seed', 'seed'
        WHERE NOT EXISTS (
            SELECT 1 FROM hd_plt_role WHERE role_name = 'MILESTONE_SCOPED')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_role_permission(role_id, permission_id, sys_create_by, sys_update_by)
        SELECT r.id, p.id, 'seed', 'seed'
        FROM hd_plt_role r JOIN hd_plt_permission p ON p.permission_code IN (
            'milestone.read', 'milestone.update')
        WHERE r.role_name = 'MILESTONE_SCOPED'
          AND NOT EXISTS (
            SELECT 1 FROM hd_plt_role_permission rp
            WHERE rp.role_id = r.id AND rp.permission_id = p.id)
        """);
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT ?, id, 'seed', 'seed'
        FROM hd_plt_role WHERE role_name = 'MILESTONE_SCOPED'
        """, userId);
    return userId;
  }

  private long seedStudy(String studyCode) {
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(
            area_code, area_name, status_code, sys_create_by, sys_update_by)
        SELECT 'TA-MS', '肿瘤', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'TA-MS')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_program(
            program_code, product_name, status_code, sys_create_by, sys_update_by)
        SELECT 'PROGRAM-MS', 'HD-MS', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (SELECT 1 FROM hd_plt_program WHERE program_code = 'PROGRAM-MS')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_project(
            project_code, program_id, indication_description, therapeutic_area_id,
            sys_create_by, sys_update_by)
        SELECT 'PROJECT-MS', p.id, '实体瘤', ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = 'PROGRAM-MS' AND ta.area_code = 'TA-MS'
          AND NOT EXISTS (SELECT 1 FROM hd_plt_project WHERE project_code = 'PROJECT-MS')
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
        WHERE p.program_code = 'PROGRAM-MS' AND pr.project_code = 'PROJECT-MS'
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
