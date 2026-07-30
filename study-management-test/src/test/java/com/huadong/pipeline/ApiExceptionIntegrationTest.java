package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huadong.pipeline.audit.AuditRequestContextFilter;
import com.huadong.pipeline.support.TestFaultController;
import com.huadong.pipeline.web.ApiErrorMessages;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestFaultController.class)
class ApiExceptionIntegrationTest {

  @Autowired MockMvc mvc;

  @Test
  void unsupportedMethodReturns405() throws Exception {
    mvc.perform(post("/api/v1/clinical-pipeline/overview")
            .with(user("admin@example.com").authorities(authority("pipeline.page.view")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
  }

  @Test
  void uncaughtExceptionReturnsUnifiedSystemMessage() throws Exception {
    mvc.perform(get("/api/v1/__test__/fault")
            .with(user("admin@example.com").authorities(authority("pipeline.page.view"))))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value(ApiErrorMessages.SYSTEM_ERROR_CODE))
        .andExpect(jsonPath("$.message").value(ApiErrorMessages.SYSTEM_ERROR_MESSAGE))
        .andExpect(header().exists(AuditRequestContextFilter.REQUEST_ID_HEADER));
  }

  @Test
  void businessExceptionReturnsChineseMessage() throws Exception {
    mvc.perform(delete("/api/v1/clinical-pipeline/programs/999999")
            .with(user("admin@example.com").authorities(authority("config.delete")))
            .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PROGRAM_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("Program 不存在"));
  }

  @Test
  void invalidRoleReturnsChineseBusinessMessage() throws Exception {
    String email = "invalid-role-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    mvc.perform(post("/api/v1/platform/users")
            .with(user("account-admin").authorities(authority("account.create")))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"%s","displayName":"Invalid Role","roleCodes":["NONEXISTENT_ROLE"]}
                """.formatted(email)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value("INVALID_ROLE"))
        .andExpect(jsonPath("$.message").value("角色不存在或已停用"));
  }

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
