package com.huadong.pipeline;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PipelineConfigIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired ObjectMapper objectMapper;

  @BeforeEach
  void resetPipelineData() {
    jdbc.update("DELETE FROM hd_plt_team_assignment");
    jdbc.update("DELETE FROM hd_plt_study");
    jdbc.update("DELETE FROM hd_plt_project");
    jdbc.update("DELETE FROM hd_plt_program");
    jdbc.update("""
        INSERT INTO hd_plt_therapeutic_area(
            area_code, area_name, status_code, sys_create_by, sys_update_by)
        SELECT 'ONCOLOGY', '肿瘤', 'ACTIVE', 'seed', 'seed'
        WHERE NOT EXISTS (
          SELECT 1 FROM hd_plt_therapeutic_area WHERE area_code = 'ONCOLOGY')
        """);
  }

  @Test
  void listsDefaultTherapeuticAreasAndCreatesEntitiesWithoutSeparateNames() throws Exception {
    mvc.perform(get("/api/v1/clinical-pipeline/therapeutic-areas")
            .with(authority("config.page.view")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
        .andExpect(jsonPath("$[*].code", hasItems(
            "ONCOLOGY", "AUTOIMMUNE", "METABOLIC_CARDIOVASCULAR")));

    String programBody = mvc.perform(post("/api/v1/clinical-pipeline/programs")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"PRG-005","productName":"HD-005","moa":"ADC",
                 "sourceCode":"SELF_DEVELOPED","originCode":"DOMESTIC"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("PRG-005"))
        .andReturn().getResponse().getContentAsString();
    long programId = objectMapper.readTree(programBody).path("id").asLong();

    mvc.perform(post("/api/v1/clinical-pipeline/projects")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"PRJ-005","programId":%d,"indication":"晚期实体瘤",
                 "therapeuticAreaCode":"ONCOLOGY"}
                """.formatted(programId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("PRJ-005"));
  }

  @Test
  void createsEntitiesAndReturnsStudyGrainedConfiguration() throws Exception {
    long programId = createProgram("PRG-001", "肿瘤项目集");
    long projectId = createProject(programId, "PRJ-001", "肺癌项目");

    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"STD-001","name":"一线临床研究","projectId":%d,
                 "phase":"PHASE_1"}
                """.formatted(projectId)))
        .andExpect(status().isCreated());

    mvc.perform(get("/api/v1/clinical-pipeline/pipeline-config")
            .with(authority("config.page.view")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].programCode").value("PRG-001"))
        .andExpect(jsonPath("$[0].projectCode").value("PRJ-001"))
        .andExpect(jsonPath("$[0].studyCode").value("STD-001"))
        .andExpect(jsonPath("$[0].projectStatus").doesNotExist());
  }

  @Test
  void previewsRenameImpactAndRequiresFreshConfirmation() throws Exception {
    long programId = createProgram("PRG-002", "旧名称");
    createProject(programId, "PRJ-002", "关联项目");

    String previewBody = mvc.perform(post("/api/v1/clinical-pipeline/programs/{id}/rename-impact", programId)
            .with(authority("config.update")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"newName\":\"新名称\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.projectCount").value(1))
        .andExpect(jsonPath("$.studyCount").value(0))
        .andReturn().getResponse().getContentAsString();
    String expectedUpdatedAt = objectMapper.readTree(previewBody).path("expectedUpdatedAt").asText();

    mvc.perform(patch("/api/v1/clinical-pipeline/programs/{id}", programId)
            .with(authority("config.update")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"新名称\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RENAME_CONFIRMATION_REQUIRED"));

    mvc.perform(patch("/api/v1/clinical-pipeline/programs/{id}", programId)
            .with(authority("config.update")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"新名称","confirmRename":true,"expectedUpdatedAt":"%s",
                 "expectedProjectCount":1,"expectedStudyCount":0}
                """.formatted(expectedUpdatedAt)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("新名称"));
  }

  @Test
  void rejectsRenameWhenReferencesChangedAfterPreview() throws Exception {
    long programId = createProgram("PRG-004", "待改名项目集");
    String previewBody = mvc.perform(post(
            "/api/v1/clinical-pipeline/programs/{id}/rename-impact", programId)
            .with(authority("config.update")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"newName\":\"新项目集名称\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    String expectedUpdatedAt = objectMapper.readTree(previewBody).path("expectedUpdatedAt").asText();

    createProject(programId, "PRJ-004", "预览后新增项目");

    mvc.perform(patch("/api/v1/clinical-pipeline/programs/{id}", programId)
            .with(authority("config.update")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"新项目集名称","confirmRename":true,"expectedUpdatedAt":"%s",
                 "expectedProjectCount":0,"expectedStudyCount":0}
                """.formatted(expectedUpdatedAt)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RENAME_IMPACT_CHANGED"));
  }

  @Test
  void rejectsDeletingReferencedProjectWithConflictDetails() throws Exception {
    long programId = createProgram("PRG-003", "引用项目集");
    long projectId = createProject(programId, "PRJ-003", "被引用项目");
    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"STD-003","name":"引用研究","projectId":%d,
                 "phase":"PHASE_2"}
                """.formatted(projectId)))
        .andExpect(status().isCreated());

    mvc.perform(delete("/api/v1/clinical-pipeline/projects/{id}", projectId)
            .with(authority("config.delete")).with(csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("PROJECT_IN_USE"))
        .andExpect(jsonPath("$.details.studyCount").value("1"));
  }

  @Test
  void rejectsConfigurationAccessWithoutItsPermission() throws Exception {
    mvc.perform(get("/api/v1/clinical-pipeline/pipeline-config")
            .with(user("reader").authorities(new SimpleGrantedAuthority("study.read"))))
        .andExpect(status().isForbidden());
  }

  private long createProgram(String code, String name) throws Exception {
    String response = mvc.perform(post("/api/v1/clinical-pipeline/programs")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"%s","name":"%s","productName":"HD-001",
                 "moa":"PD-1","sourceCode":"SELF_DEVELOPED","originCode":"DOMESTIC"}
                """.formatted(code, name)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    JsonNode json = objectMapper.readTree(response);
    return json.path("id").asLong();
  }

  private long createProject(long programId, String code, String name) throws Exception {
    String response = mvc.perform(post("/api/v1/clinical-pipeline/projects")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"%s","name":"%s","programId":%d,
                 "indication":"非小细胞肺癌","therapeuticAreaCode":"ONCOLOGY"}
                """.formatted(code, name, programId)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response).path("id").asLong();
  }

  private static org.springframework.test.web.servlet.request.RequestPostProcessor authority(
      String permission) {
    return user("config-admin").authorities(new SimpleGrantedAuthority(permission));
  }
}
