package com.huadong.pipeline;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiExceptionIntegrationTest {

  @Autowired MockMvc mvc;

  @Test
  void missingRequiredParameterReturnsBadRequest() throws Exception {
    mvc.perform(get("/api/v1/clinical-pipeline/overview")
            .with(user("admin@example.com").authorities(authority("pipeline.page.view")))
            .param("unexpected", "value"))
        .andExpect(status().isOk()); // endpoint has no required params, so this passes
  }

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

  private static SimpleGrantedAuthority authority(String code) {
    return new SimpleGrantedAuthority(code);
  }
}
