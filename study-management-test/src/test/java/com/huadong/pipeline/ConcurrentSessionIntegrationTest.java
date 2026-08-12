package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConcurrentSessionIntegrationTest {
  private static final String ADMIN = "admin@example.com";
  private static final String ADMIN_PASSWORD = "Admin-Change-Me-123!";

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired PlatformTransactionManager transactionManager;

  @Test
  void loginInvalidatesExistingSessionsForTheSameUser() throws Exception {
    String oldSessionId = UUID.randomUUID().toString();
    long now = System.currentTimeMillis();
    var sessionTransaction = new TransactionTemplate(transactionManager);
    sessionTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    sessionTransaction.executeWithoutResult(ignored -> jdbc.update("""
            INSERT INTO hd_plt_spring_session(
                primary_id, session_id, creation_time, last_access_time,
                max_inactive_interval, expiry_time, principal_name)
            VALUES (?, ?, ?, ?, 1800, ?, ?)
            """, oldSessionId, oldSessionId, now, now, now + 1_800_000, ADMIN));

    mvc.perform(post("/api/v1/platform/auth/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", ADMIN)
            .param("password", ADMIN_PASSWORD)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("登录成功"));

    Integer remaining = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_spring_session WHERE session_id = ?",
        Integer.class,
        oldSessionId);
    assertThat(remaining).isZero();
  }

  @Test
  void secondLoginMakesTheFirstSessionUnauthenticated() throws Exception {
    Cookie firstCookie = loginAndSessionCookie();

    mvc.perform(get("/api/v1/platform/me").cookie(firstCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(ADMIN));

    Cookie secondCookie = loginAndSessionCookie();
    assertThat(secondCookie.getValue()).isNotEqualTo(firstCookie.getValue());

    mvc.perform(get("/api/v1/platform/me").cookie(firstCookie))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

    mvc.perform(get("/api/v1/platform/me").cookie(secondCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(ADMIN));
  }

  private Cookie loginAndSessionCookie() throws Exception {
    MvcResult result = mvc.perform(post("/api/v1/platform/auth/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", ADMIN)
            .param("password", ADMIN_PASSWORD)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("SESSION"))
        .andReturn();
    Cookie sessionCookie = result.getResponse().getCookie("SESSION");
    assertThat(sessionCookie).isNotNull();
    return sessionCookie;
  }
}
