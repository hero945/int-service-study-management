package com.huadong.pipeline;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoleManagementIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired ObjectMapper objectMapper;
  @Autowired PlatformTransactionManager transactionManager;

  @Test
  void roleNamesDoNotBypassRoleManagementPermissions() throws Exception {
    mvc.perform(get("/api/v1/platform/roles").with(user("admin").roles("ADMIN")))
        .andExpect(status().isForbidden());
  }

  @Test
  void listsRolesAndTheReadOnlyPermissionDictionary() throws Exception {
    mvc.perform(get("/api/v1/platform/roles")
            .with(user("role-auditor").authorities(authority("role.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[?(@.roleCode == 'ADMIN')]", hasSize(1)))
        .andExpect(jsonPath("$.data[?(@.roleCode == 'ADMIN')].systemRole", hasItem(true)));

    mvc.perform(get("/api/v1/platform/permissions")
            .with(user("role-auditor").authorities(authority("role.page.view"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].permissionCode", hasItem("role.page.view")))
        .andExpect(jsonPath("$[*].permissionCode", hasItem("role.delete")));
  }

  @Test
  void createsAndUpdatesRolePermissionsAtomically() throws Exception {
    mvc.perform(post("/api/v1/platform/roles")
            .with(user("role-admin").authorities(authority("role.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleCode":"CLINICAL_LEAD","roleDescription":"Clinical lead",
                 "dataScopeMode":"ASSIGNED_STUDY",
                 "permissionCodes":["pipeline.page.view","study.read"]}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.roleCode").value("CLINICAL_LEAD"))
        .andExpect(jsonPath("$.permissionCodes", hasSize(2)));

    long roleId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_role WHERE role_name = 'CLINICAL_LEAD'", Long.class);

    mvc.perform(put("/api/v1/platform/roles/{roleId}", roleId)
            .with(user("role-admin").authorities(authority("role.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleDescription":"Updated lead","dataScopeMode":"ALL","status":"ACTIVE",
                 "permissionCodes":["pipeline.page.view"]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role.permissionCodes", hasSize(1)));

    mvc.perform(put("/api/v1/platform/roles/{roleId}", roleId)
            .with(user("role-admin").authorities(authority("role.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleDescription":"Updated lead","dataScopeMode":"ALL","status":"ACTIVE",
                 "permissionCodes":["pipeline.page.view","study.read"]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role.permissionCodes", hasSize(2)));

    Integer auditCount = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_audit_log WHERE target_table = 'hd_plt_role' AND target_id = ?",
        Integer.class,
        roleId);
    org.assertj.core.api.Assertions.assertThat(auditCount).isEqualTo(3);
  }

  @Test
  void rejectsSystemRoleDeletionAndDeletingRolesAssignedToUsers() throws Exception {
    long adminRoleId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_role WHERE role_name = 'ADMIN'", Long.class);

    mvc.perform(delete("/api/v1/platform/roles/{roleId}", adminRoleId)
            .with(user("role-admin").authorities(authority("role.delete"))).with(csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SYSTEM_ROLE_PROTECTED"));

    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code, is_system_role,
            sys_create_by, sys_update_by)
        VALUES ('ASSIGNED_ROLE', 'Assigned', 'ASSIGNED_STUDY', 'ACTIVE', 0, 'seed', 'seed')
        """);
    long assignedRoleId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_role WHERE role_name = 'ASSIGNED_ROLE'", Long.class);
    jdbc.update("""
        INSERT INTO hd_plt_user_role(user_id, role_id, sys_create_by, sys_update_by)
        SELECT id, ?, 'seed', 'seed' FROM hd_plt_user WHERE email = 'admin@example.com'
        """, assignedRoleId);
    mvc.perform(delete("/api/v1/platform/roles/{roleId}", assignedRoleId)
            .with(user("role-admin").authorities(authority("role.delete"))).with(csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ROLE_IN_USE"));
  }

  @Test
  void preventsRemovingTheLastEffectiveRoleAdministrator() throws Exception {
    long adminRoleId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_role WHERE role_name = 'ADMIN'", Long.class);

    mvc.perform(put("/api/v1/platform/roles/{roleId}", adminRoleId)
            .with(user("role-admin").authorities(authority("role.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleDescription":"System administrator","dataScopeMode":"ALL","status":"ACTIVE",
                 "permissionCodes":["pipeline.page.view","study.read"]}
                """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("LAST_ROLE_ADMIN_PROTECTED"));
  }

  @Test
  void invalidatesSessionsOfUsersAssignedToAnUpdatedRole() throws Exception {
    long adminRoleId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_role WHERE role_name = 'ADMIN'", Long.class);
    var permissionCodes = jdbc.queryForList("""
        SELECT p.permission_code
        FROM hd_plt_role_permission rp
        JOIN hd_plt_permission p ON p.id = rp.permission_id
        WHERE rp.role_id = ? AND rp.sys_deleted = 0 AND p.sys_deleted = 0
        ORDER BY p.sort_order, p.permission_code
        """, String.class, adminRoleId);

    String sessionId = UUID.randomUUID().toString();
    long now = System.currentTimeMillis();
    var sessionTransaction = new TransactionTemplate(transactionManager);
    sessionTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    sessionTransaction.executeWithoutResult(ignored -> jdbc.update("""
            INSERT INTO hd_plt_spring_session(
                primary_id, session_id, creation_time, last_access_time,
                max_inactive_interval, expiry_time, principal_name)
            VALUES (?, ?, ?, ?, 1800, ?, 'admin@example.com')
            """, sessionId, sessionId, now, now, now + 1_800_000));

    String request = objectMapper.writeValueAsString(Map.of(
        "roleDescription", "System administrator",
        "dataScopeMode", "ASSIGNED_STUDY",
        "status", "ACTIVE",
        "permissionCodes", permissionCodes));

    mvc.perform(put("/api/v1/platform/roles/{roleId}", adminRoleId)
            .with(user("admin@example.com").authorities(authority("role.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invalidatedUserCount").value(1))
        .andExpect(jsonPath("$.currentSessionInvalidated").value(true));

    Integer remaining = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_spring_session WHERE session_id = ?",
        Integer.class,
        sessionId);
    org.assertj.core.api.Assertions.assertThat(remaining).isZero();
  }

  @Test
  void logicallyDeletesUnusedCustomRoles() throws Exception {
    jdbc.update("""
        INSERT INTO hd_plt_role(
            role_name, role_description, data_scope_mode, status_code, is_system_role,
            sys_create_by, sys_update_by)
        VALUES ('TEMP_ROLE', 'Temporary', 'ASSIGNED_STUDY', 'ACTIVE', 0, 'seed', 'seed')
        """);
    long roleId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_role WHERE role_name = 'TEMP_ROLE'", Long.class);

    mvc.perform(delete("/api/v1/platform/roles/{roleId}", roleId)
            .with(user("role-admin").authorities(authority("role.delete"))).with(csrf()))
        .andExpect(status().isNoContent());

    Integer deleted = jdbc.queryForObject(
        "SELECT sys_deleted FROM hd_plt_role WHERE id = ?", Integer.class, roleId);
    org.assertj.core.api.Assertions.assertThat(deleted).isEqualTo(1);
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
