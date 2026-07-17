package com.huadong.pipeline;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformIntegrationTest {
    @Autowired MockMvc mvc;

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
    void createsStudyAndReturnsServerOwnedDisplayFields() throws Exception {
        mvc.perform(post("/api/v1/clinical-pipeline/studies")
                        .with(user("researcher").roles("USER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"HD-MVP-001","name":"MVP 临床研究","indication":"实体瘤",
                                 "phase":"I期","status":"ACTIVE","ownerName":"张三","startDate":"2026-07-17"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/clinical-pipeline/studies").with(user("researcher").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].statusLabel").value("进行中"))
                .andExpect(jsonPath("$[0].statusTone").value("positive"));
        mvc.perform(get("/api/v1/clinical-pipeline/overview").with(user("researcher").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("临床研发管线"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void onlyAdminMayReadOrChangeDynamicSettings() throws Exception {
        mvc.perform(get("/api/v1/platform/settings").with(user("viewer").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/platform/settings?key=platform.display-name")
                        .with(user("admin").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"研发运营平台\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("研发运营平台"));
    }

    @Test
    void rejectsWriteWithoutCsrf() throws Exception {
        mvc.perform(post("/api/v1/clinical-pipeline/studies")
                        .with(user("researcher").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminMayCreateUsersAndPasswordHashesStayInternal() throws Exception {
        mvc.perform(get("/api/v1/platform/users").with(user("viewer").roles("USER")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/platform/users")
                        .with(user("admin").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test.user","displayName":"测试用户",
                                 "password":"Test-Password-123!","role":"USER"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/platform/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'test.user')]", hasSize(1)))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }
}
