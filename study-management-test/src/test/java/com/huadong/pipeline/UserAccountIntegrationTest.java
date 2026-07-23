package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserAccountIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired UserDetailsService userDetailsService;

  @Test
  void reassigningRolesDoesNotCauseDuplicateEntry() throws Exception {
    String email = "reassign-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    mvc.perform(post("/api/v1/platform/users")
            .with(user("account-admin").authorities(authority("account.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"%s","displayName":"Reassign Tester","roleCodes":["USER"]}
                """.formatted(email)))
        .andExpect(status().isCreated());

    long userId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);

    // 重复分配相同角色：旧实现会先软删再插入，触发 Duplicate entry
    mvc.perform(put("/api/v1/platform/users/{id}/roles", userId)
            .with(user("account-admin").authorities(authority("account.assignRole"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleCodes":["USER"]}
                """))
        .andExpect(status().isOk());

    // 移除再重新分配同一角色：验证软删除后仍能重新插入有效行
    mvc.perform(put("/api/v1/platform/users/{id}/roles", userId)
            .with(user("account-admin").authorities(authority("account.assignRole"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleCodes":["USER","VIEWER"]}
                """))
        .andExpect(status().isOk());
    mvc.perform(put("/api/v1/platform/users/{id}/roles", userId)
            .with(user("account-admin").authorities(authority("account.assignRole"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleCodes":["USER"]}
                """))
        .andExpect(status().isOk());
    mvc.perform(put("/api/v1/platform/users/{id}/roles", userId)
            .with(user("account-admin").authorities(authority("account.assignRole"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roleCodes":["USER","VIEWER"]}
                """))
        .andExpect(status().isOk());

    Integer activeRoles = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_user_role WHERE user_id = ? AND sys_deleted = 0",
        Integer.class, userId);
    assertThat(activeRoles).isEqualTo(2);
  }

  @Test
  void disablingUserBlocksLoginAndKeepsRowInList() throws Exception {
    String email = "disable-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    mvc.perform(post("/api/v1/platform/users")
            .with(user("account-admin").authorities(authority("account.create"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"%s","displayName":"Disable Tester","roleCodes":["USER"]}
                """.formatted(email)))
        .andExpect(status().isCreated());

    long userId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);

    // 停用：通过 update 将 enabled 置为 false（status_code = DISABLED，sys_deleted 不变）
    mvc.perform(patch("/api/v1/platform/users/{id}", userId)
            .with(user("account-admin").authorities(authority("account.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"displayName":"Disable Tester","enabled":false}
                """))
        .andExpect(status().isOk());

    // 1) 禁用用户无法登录：UserDetails 处于禁用状态
    var details = userDetailsService.loadUserByUsername(email);
    assertThat(details.isEnabled()).isFalse();

    // 2) 用户仍在列表（sys_deleted 不改为 1），仅状态变为 DISABLED
    Integer sysDeleted = jdbc.queryForObject(
        "SELECT sys_deleted FROM hd_plt_user WHERE id = ?", Integer.class, userId);
    String statusCode = jdbc.queryForObject(
        "SELECT status_code FROM hd_plt_user WHERE id = ?", String.class, userId);
    assertThat(sysDeleted).isEqualTo(0);
    assertThat(statusCode).isEqualTo("DISABLED");

    // 重新启用后恢复登录
    mvc.perform(patch("/api/v1/platform/users/{id}", userId)
            .with(user("account-admin").authorities(authority("account.update"))).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"displayName":"Disable Tester","enabled":true}
                """))
        .andExpect(status().isOk());
    assertThat(userDetailsService.loadUserByUsername(email).isEnabled()).isTrue();
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
