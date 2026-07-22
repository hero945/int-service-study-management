package com.huadong.pipeline;

import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserDetailsService userDetailsService;
    @Autowired JdbcTemplate jdbc;

    @Test
    void loadsPermissionCodesInsteadOfRoleAuthoritiesForAuthentication() {
        var authenticatedAdmin = userDetailsService.loadUserByUsername("admin@example.com");

        assertThat(authenticatedAdmin.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("account.page.view", "account.create", "study.read")
                .doesNotContain("ROLE_ADMIN");
    }

    @Test
    void protectsBusinessApiAndExposesCsrfBootstrap() throws Exception {
        mvc.perform(get("/api/v1/platform/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(get("/api/v1/clinical-pipeline/studies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void redirectsAnonymousBrowserPageRequestsToLoginAndPreservesTheTarget() throws Exception {
        for (String page : java.util.List.of(
                "/pipeline", "/config", "/team", "/accounts", "/roles")) {
            mvc.perform(get(page).accept(MediaType.TEXT_HTML))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?redirect="
                            + java.net.URLEncoder.encode(page, java.nio.charset.StandardCharsets.UTF_8)));
        }
    }

    @Test
    void forwardsSpaPageRoutesToIndexHtmlForAuthenticatedUsers() throws Exception {
        // Guard: every client-side route must be registered in SpaController,
        // otherwise a browser refresh on that page returns a 404 Whitelabel page.
        for (String page : java.util.List.of(
                "/pipeline", "/studies", "/monthly", "/reports", "/milestones/1",
                "/studies/1/monthly-report")) {
            mvc.perform(get(page).with(user("tester")))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test
    void exposesOnlyPublicSettingsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/platform/settings/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].configKey").value("platform.display-name"));
    }

    @Test
    void createsStudyAndReturnsServerOwnedDisplayFields() throws Exception {
        seedStudyHierarchy();
        mvc.perform(post("/api/v1/clinical-pipeline/studies")
                        .with(user("researcher").authorities(
                                new SimpleGrantedAuthority("config.create"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"HD-MVP-001",
                                 "programCode":"PROGRAM-001","projectCode":"PROJECT-001",
                                 "therapeuticAreaCode":"ONCOLOGY","phase":"PHASE_1",
                                 "plannedStartDate":"2026-07-17","actualStartDate":"2026-07-17"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/clinical-pipeline/studies")
                        .with(user(userDetailsService.loadUserByUsername("admin@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").doesNotExist())
                .andExpect(jsonPath("$[0].statusLabel").value("进行中"))
                .andExpect(jsonPath("$[0].statusTone").value("positive"));
        mvc.perform(get("/api/v1/clinical-pipeline/overview")
                        .with(user(userDetailsService.loadUserByUsername("admin@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("临床研发管线"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void permissionsControlReadingAndChangingDynamicSettings() throws Exception {
        mvc.perform(get("/api/v1/platform/settings").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/platform/settings?key=platform.display-name")
                        .with(user("operator").authorities(
                                new SimpleGrantedAuthority("platform.setting.update"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"研发运营平台\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("研发运营平台"));
    }

    @Test
    void rejectsWriteWithoutCsrf() throws Exception {
        mvc.perform(post("/api/v1/clinical-pipeline/studies")
                        .with(user("researcher").authorities(
                                new SimpleGrantedAuthority("config.create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminMayCreateUsersAndPasswordHashesStayInternal() throws Exception {
        mvc.perform(get("/api/v1/platform/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/platform/users")
                        .with(user("account-admin").authorities(
                                new SimpleGrantedAuthority("account.create"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test.user@example.com","displayName":"测试用户",
                                 "password":"Test-Password-123!","roleCodes":["USER","VIEWER"]}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/platform/users").with(user("account-auditor").authorities(
                        new SimpleGrantedAuthority("account.page.view"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'test.user@example.com')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.username == 'test.user@example.com')].roles[0]", hasSize(1)))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    private void seedStudyHierarchy() {
        jdbc.update("DELETE FROM hd_plt_study");
        jdbc.update("DELETE FROM hd_plt_project");
        jdbc.update("DELETE FROM hd_plt_program");
        jdbc.update("DELETE FROM hd_plt_therapeutic_area");
        jdbc.update("""
                INSERT INTO hd_plt_therapeutic_area(
                    area_code, area_name, status_code, sys_create_by, sys_update_by)
                VALUES ('ONCOLOGY', '肿瘤', 'ACTIVE', 'seed', 'seed')
                """);
        jdbc.update("""
                INSERT INTO hd_plt_program(
                    program_code, product_name, status_code,
                    sys_create_by, sys_update_by)
                VALUES ('PROGRAM-001', 'HD-001', 'ACTIVE', 'seed', 'seed')
                """);
        jdbc.update("""
                INSERT INTO hd_plt_project(
                    project_code, program_id, indication_description,
                    therapeutic_area_id, sys_create_by, sys_update_by)
                SELECT 'PROJECT-001', p.id, '实体瘤', ta.id, 'seed', 'seed'
                FROM hd_plt_program p CROSS JOIN hd_plt_therapeutic_area ta
                WHERE p.program_code = 'PROGRAM-001' AND ta.area_code = 'ONCOLOGY'
                """);
    }
}
