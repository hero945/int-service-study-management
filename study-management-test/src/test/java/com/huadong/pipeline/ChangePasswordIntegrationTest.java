package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChangePasswordIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordEncoder passwordEncoder;

  @Test
  void createUserUsesDefaultPasswordAndUserCanChangeIt() throws Exception {
    String email = "pwd-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    mvc.perform(post("/api/v1/platform/users")
            .with(user("account-admin").authorities(new SimpleGrantedAuthority("account.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"%s","displayName":"Pwd Tester","roleCodes":["USER"]}
                """.formatted(email)))
        .andExpect(status().isCreated());

    String hash = jdbc.queryForObject(
        "SELECT password_hash FROM hd_plt_user WHERE email = ?", String.class, email);
    assertThat(passwordEncoder.matches("Hd123456", hash)).isTrue();

    mvc.perform(post("/api/v1/platform/me/password")
            .with(user(email).roles("USER")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"currentPassword":"Hd123456","newPassword":"NewPass12"}
                """))
        .andExpect(status().isNoContent());

    String updated = jdbc.queryForObject(
        "SELECT password_hash FROM hd_plt_user WHERE email = ?", String.class, email);
    assertThat(passwordEncoder.matches("NewPass12", updated)).isTrue();
  }

  @Test
  void rejectsWrongCurrentPasswordAndWeakNewPassword() throws Exception {
    String email = "pwd2-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    mvc.perform(post("/api/v1/platform/users")
            .with(user("account-admin").authorities(new SimpleGrantedAuthority("account.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"%s","displayName":"Pwd Tester 2","roleCodes":["USER"]}
                """.formatted(email)))
        .andExpect(status().isCreated());

    mvc.perform(post("/api/v1/platform/me/password")
            .with(user(email).roles("USER")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"currentPassword":"wrong-password","newPassword":"NewPass12"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_CURRENT_PASSWORD"));

    mvc.perform(post("/api/v1/platform/me/password")
            .with(user(email).roles("USER")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"currentPassword":"Hd123456","newPassword":"short"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  @Test
  void adminCanResetPasswordToDefault() throws Exception {
    String email = "pwd3-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    mvc.perform(post("/api/v1/platform/users")
            .with(user("account-admin").authorities(new SimpleGrantedAuthority("account.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"%s","displayName":"Pwd Tester 3","roleCodes":["USER"]}
                """.formatted(email)))
        .andExpect(status().isCreated());

    mvc.perform(post("/api/v1/platform/me/password")
            .with(user(email).roles("USER")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"currentPassword":"Hd123456","newPassword":"NewPass12"}
                """))
        .andExpect(status().isNoContent());

    long userId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_user WHERE email = ?", Long.class, email);

    mvc.perform(post("/api/v1/platform/users/{id}/password-reset", userId)
            .with(user("account-admin").authorities(new SimpleGrantedAuthority("account.update")))
            .with(csrf()))
        .andExpect(status().isNoContent());

    String hash = jdbc.queryForObject(
        "SELECT password_hash FROM hd_plt_user WHERE email = ?", String.class, email);
    assertThat(passwordEncoder.matches("Hd123456", hash)).isTrue();
  }
}
