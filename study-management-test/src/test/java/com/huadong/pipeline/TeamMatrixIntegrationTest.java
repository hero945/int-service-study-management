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
class TeamMatrixIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void listsThePaginatedMatrixAndReplacesAssignmentsAtomically() throws Exception {
    long studyId = seedStudy("TEAM-STUDY-001");
    long memberId = seedUser("team.member@example.com", "团队成员", true);

    mvc.perform(get("/api/v1/team-matrix")
            .param("studyQuery", "TEAM-STUDY-001")
            .param("page", "1")
            .param("pageSize", "20")
            .with(user("admin@example.com").authorities(authority("team.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roles.length()").value(44))
        .andExpect(jsonPath("$.studies[0].studyCode").value("TEAM-STUDY-001"))
        .andExpect(jsonPath("$.pagination.pageSize").value(20));

    mvc.perform(put("/api/v1/team-matrix/assignments")
            .with(user("admin@example.com").authorities(
                authority("team.edit_mode"), authority("team.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"studies":[{"studyId":%d,"expectedVersion":0,
                  "roles":[{"roleCode":"PL","userIds":[%d]}]}]}
                """.formatted(studyId, memberId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studies[0].studyId").value(studyId))
        .andExpect(jsonPath("$.studies[0].version").value(1));

    mvc.perform(get("/api/v1/team-matrix")
            .param("studyQuery", "TEAM-STUDY-001")
            .with(user("admin@example.com").authorities(authority("team.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assignments[0].roleCode").value("PL"))
        .andExpect(jsonPath("$.assignments[0].members[0].displayName").value("团队成员"));

    Integer auditCount = jdbc.queryForObject("""
        SELECT COUNT(*) FROM hd_plt_audit_log
        WHERE target_table = 'hd_plt_team_assignment' AND target_id = ?
        """, Integer.class, studyId);
    org.assertj.core.api.Assertions.assertThat(auditCount).isEqualTo(1);
  }

  @Test
  void rejectsStaleVersionsWithoutChangingAssignments() throws Exception {
    long studyId = seedStudy("TEAM-STUDY-002");
    long firstUser = seedUser("first.member@example.com", "第一成员", true);
    long secondUser = seedUser("second.member@example.com", "第二成员", true);
    replace(studyId, 0, firstUser).andExpect(status().isOk());

    replace(studyId, 0, secondUser)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("TEAM_VERSION_CONFLICT"));

    Integer assignments = jdbc.queryForObject("""
        SELECT COUNT(*) FROM hd_plt_team_assignment ta
        JOIN hd_plt_user u ON u.id = ta.user_id
        WHERE ta.study_id = ? AND ta.sys_deleted = 0 AND u.email = 'first.member@example.com'
        """, Integer.class, studyId);
    org.assertj.core.api.Assertions.assertThat(assignments).isEqualTo(1);
  }

  @Test
  void rejectsDisabledNewMembersAndMissingPermissions() throws Exception {
    long studyId = seedStudy("TEAM-STUDY-003");
    long disabledUser = seedUser("disabled.member@example.com", "停用成员", false);

    replace(studyId, 0, disabledUser)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value("INVALID_TEAM_MEMBER"));

    mvc.perform(get("/api/v1/team-matrix").with(user("admin@example.com")))
        .andExpect(status().isForbidden());

    mvc.perform(get("/team")
            .accept(MediaType.TEXT_HTML)
            .with(user("admin@example.com")))
        .andExpect(status().is3xxRedirection());
    mvc.perform(get("/team")
            .accept(MediaType.TEXT_HTML)
            .with(user("admin@example.com").authorities(authority("team.page.view"))))
        .andExpect(status().isOk());
  }

  @Test
  void rejectsNullMemberListsInsteadOfTreatingThemAsRemoval() throws Exception {
    long studyId = seedStudy("TEAM-STUDY-007");

    mvc.perform(put("/api/v1/team-matrix/assignments")
            .with(user("admin@example.com").authorities(
                authority("team.edit_mode"), authority("team.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"studies":[{"studyId":%d,"expectedVersion":0,
                  "roles":[{"roleCode":"PL","userIds":null}]}]}
                """.formatted(studyId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void assignedUserCanRemoveTheLastOwnAssignmentAndImmediatelyLosesVisibility()
      throws Exception {
    long studyId = seedStudy("TEAM-STUDY-004");
    long memberId = seedUser("scoped.member@example.com", "范围成员", true);
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code,
            is_system_role, sys_create_by, sys_update_by)
        VALUES ('TEAM_SCOPED_TEST', 'Assigned Study test role', 'ASSIGNED_STUDY',
            'ACTIVE', 0, 'seed', 'seed')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT ?, id, 'seed', 'seed'
        FROM hd_plt_role WHERE role_name = 'TEAM_SCOPED_TEST'
        """, memberId);
    replace(studyId, 0, memberId).andExpect(status().isOk());

    mvc.perform(get("/api/v1/team-matrix")
            .with(user("scoped.member@example.com").authorities(authority("team.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pagination.totalItems").value(1));

    mvc.perform(put("/api/v1/team-matrix/assignments")
            .with(user("scoped.member@example.com").authorities(
                authority("team.edit_mode"), authority("team.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"studies":[{"studyId":%d,"expectedVersion":1,
                  "roles":[{"roleCode":"PL","userIds":[]}]}]}
                """.formatted(studyId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studies[0].version").value(2));

    mvc.perform(get("/api/v1/team-matrix")
            .with(user("scoped.member@example.com").authorities(authority("team.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pagination.totalItems").value(0));
  }

  @Test
  void staleStudyRollsBackTheWholeMultiStudyBatch() throws Exception {
    long firstStudy = seedStudy("TEAM-STUDY-005");
    long secondStudy = seedStudy("TEAM-STUDY-006");
    long memberId = seedUser("batch.member@example.com", "批量成员", true);
    replace(secondStudy, 0, memberId).andExpect(status().isOk());

    mvc.perform(put("/api/v1/team-matrix/assignments")
            .with(user("admin@example.com").authorities(
                authority("team.edit_mode"), authority("team.update")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"studies":[
                  {"studyId":%d,"expectedVersion":0,
                   "roles":[{"roleCode":"PL","userIds":[%d]}]},
                  {"studyId":%d,"expectedVersion":0,
                   "roles":[{"roleCode":"PM","userIds":[%d]}]}
                ]}
                """.formatted(firstStudy, memberId, secondStudy, memberId)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("TEAM_VERSION_CONFLICT"));

    Integer firstAssignments = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_team_assignment WHERE study_id = ? AND sys_deleted = 0",
        Integer.class, firstStudy);
    org.assertj.core.api.Assertions.assertThat(firstAssignments).isZero();
  }

  @Test
  void studyDrawerTeamForUnknownStudyReturnsNotFound() throws Exception {
    mvc.perform(get("/api/v1/studies/{id}/team", 999999L)
            .with(user("admin@example.com").authorities(authority("study.read"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("STUDY_NOT_FOUND"));
  }

  @Test
  void studyDrawerTeamFallsBackToStudyStatusWhenMilestoneReadMissing() throws Exception {
    long studyId = seedStudy("TEAM-STUDY-011");
    String viewer = "team.viewer@example.com";
    seedUserWithPermission(viewer, "team viewer", "study.read");
    mvc.perform(get("/api/v1/studies/{id}/team", studyId)
            .with(user(viewer).authorities(authority("study.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studies[0].currentStatus").value("计划中"));
  }

  private void seedUserWithPermission(String email, String displayName, String permissionCode) {
    jdbc.update("""
        INSERT INTO hd_plt_user(
            email, password_hash, display_name, status_code, security_stamp,
            sys_create_by, sys_update_by)
        VALUES (?, 'hash', ?, 'ACTIVE', ?, 'seed', 'seed')
        """, email, displayName, UUID.randomUUID().toString());
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

  @Test
  void studyDrawerTeamIsReadableWithStudyReadWithoutTeamPageView() throws Exception {
    long studyId = seedStudy("TEAM-STUDY-008");
    long memberId = seedUser("drawer.member@example.com", "抽屉成员", true);
    replace(studyId, 0, memberId).andExpect(status().isOk());

    mvc.perform(get("/api/v1/studies/{id}/team", studyId)
            .with(user("admin@example.com").authorities(authority("study.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studies[0].studyCode").value("TEAM-STUDY-008"))
        .andExpect(jsonPath("$.roles.length()").value(44))
        .andExpect(jsonPath("$.assignments[0].roleCode").value("PL"))
        .andExpect(jsonPath("$.assignments[0].members[0].displayName").value("抽屉成员"));

    mvc.perform(get("/api/v1/team-matrix")
            .with(user("admin@example.com").authorities(authority("study.read"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void studyDrawerTeamOutsideAssignedScopeIsForbidden() throws Exception {
    long inScope = seedStudy("TEAM-STUDY-009");
    long outOfScope = seedStudy("TEAM-STUDY-010");
    long memberId = seedUser("drawer.scoped@example.com", "抽屉范围成员", true);
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code,
            is_system_role, sys_create_by, sys_update_by)
        VALUES ('TEAM_DRAWER_SCOPED', 'Drawer assigned study', 'ASSIGNED_STUDY',
            'ACTIVE', 0, 'seed', 'seed')
        """);
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT ?, id, 'seed', 'seed'
        FROM hd_plt_role WHERE role_name = 'TEAM_DRAWER_SCOPED'
        """, memberId);
    replace(inScope, 0, memberId).andExpect(status().isOk());

    mvc.perform(get("/api/v1/studies/{id}/team", inScope)
            .with(user("drawer.scoped@example.com").authorities(authority("study.read"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studies[0].studyId").value(inScope));

    mvc.perform(get("/api/v1/studies/{id}/team", outOfScope)
            .with(user("drawer.scoped@example.com").authorities(authority("study.read"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("STUDY_OUT_OF_SCOPE"));
  }

  private org.springframework.test.web.servlet.ResultActions replace(
      long studyId, long version, long userId) throws Exception {
    return mvc.perform(put("/api/v1/team-matrix/assignments")
        .with(user("admin@example.com").authorities(
            authority("team.edit_mode"), authority("team.update")))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"studies":[{"studyId":%d,"expectedVersion":%d,
              "roles":[{"roleCode":"PL","userIds":[%d]}]}]}
            """.formatted(studyId, version, userId)));
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

  private long seedStudy(String studyCode) {
    String suffix = studyCode.substring(studyCode.length() - 3);
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(
            area_code, area_name, status_code, sys_create_by, sys_update_by)
        VALUES (?, '肿瘤', 'ACTIVE', 'seed', 'seed')
        """, "TA-TEAM-" + suffix);
    jdbc.update("""
        INSERT INTO hd_plt_program(
            program_code, product_name, status_code, sys_create_by, sys_update_by)
        VALUES (?, ?, 'ACTIVE', 'seed', 'seed')
        """, "PROGRAM-TEAM-" + suffix, "HD-TEAM-" + suffix);
    jdbc.update("""
        INSERT INTO hd_plt_project(
            project_code, program_id, indication_description, therapeutic_area_id,
            sys_create_by, sys_update_by)
        SELECT ?, p.id, '实体瘤', ta.id, 'seed', 'seed'
        FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
        WHERE p.program_code = ? AND ta.area_code = ?
        """, "PROJECT-TEAM-" + suffix,
        "PROGRAM-TEAM-" + suffix, "TA-TEAM-" + suffix);
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
        FROM hd_plt_program p
        JOIN hd_plt_project pr ON pr.program_id = p.id
        JOIN hd_plt_therapeutic_area ta ON ta.id = pr.therapeutic_area_id
        WHERE p.program_code = ? AND pr.project_code = ?
        """, studyCode, "PROGRAM-TEAM-" + suffix, "PROJECT-TEAM-" + suffix);
    return jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = ?", Long.class, studyCode);
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
