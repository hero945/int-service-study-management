package com.huadong.pipeline;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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
        .andExpect(jsonPath("$.name").doesNotExist())
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
        .andExpect(jsonPath("$.name").doesNotExist());
  }

  @Test
  void createsEntitiesAndReturnsStudyGrainedConfiguration() throws Exception {
    long programId = createProgram("PRG-001");
    long projectId = createProject(programId, "PRJ-001");

    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"STD-001","projectId":%d,
                 "phase":"PHASE_1"}
                """.formatted(projectId)))
        .andExpect(status().isCreated());

    mvc.perform(get("/api/v1/clinical-pipeline/pipeline-config")
            .with(authority("config.page.view")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.pageSize").value(10))
        .andExpect(jsonPath("$.totalItems").value(1))
        .andExpect(jsonPath("$.data[0].programCode").value("PRG-001"))
        .andExpect(jsonPath("$.data[0].projectCode").value("PRJ-001"))
        .andExpect(jsonPath("$.data[0].studyCode").value("STD-001"))
        .andExpect(jsonPath("$.data[0].studyName").doesNotExist())
        .andExpect(jsonPath("$.data[0].programName").doesNotExist())
        .andExpect(jsonPath("$.data[0].projectName").doesNotExist())
        .andExpect(jsonPath("$.data[0].phaseStatusLabel").doesNotExist())
        .andExpect(jsonPath("$.data[0].projectStatus").doesNotExist());
  }

  @Test
  void rejectsDeletingReferencedProjectWithConflictDetails() throws Exception {
    long programId = createProgram("PRG-003");
    long projectId = createProject(programId, "PRJ-003");
    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"STD-003","projectId":%d,
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

  @Test
  void listsPipelineConfigWithServerSidePagingAndKeyword() throws Exception {
    long programId = createProgram("PRG-PAGE");
    long projectId = createProject(programId, "PRJ-PAGE");
    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"STD-PAGE-A","projectId":%d,"phase":"PHASE_1"}
                """.formatted(projectId)))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"STD-PAGE-B","projectId":%d,"phase":"PHASE_2"}
                """.formatted(projectId)))
        .andExpect(status().isCreated());

    mvc.perform(get("/api/v1/clinical-pipeline/pipeline-config")
            .param("page", "1")
            .param("pageSize", "1")
            .with(authority("config.page.view")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.totalItems").value(2))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andExpect(jsonPath("$.pageSize").value(1));

    mvc.perform(get("/api/v1/clinical-pipeline/pipeline-config")
            .param("keyword", "STD-PAGE-B")
            .with(authority("config.page.view")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].studyCode").value("STD-PAGE-B"))
        .andExpect(jsonPath("$.totalItems").value(1));
  }

  @Test
  void previewsAndDeletesStudyWithRelatedData() throws Exception {
    long programId = createProgram("PRG-DEL");
    long projectId = createProject(programId, "PRJ-DEL");
    mvc.perform(post("/api/v1/clinical-pipeline/studies")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"STD-DEL","projectId":%d,"phase":"PHASE_1"}
                """.formatted(projectId)))
        .andExpect(status().isCreated());

    long studyId = jdbc.queryForObject(
        "SELECT id FROM hd_plt_study WHERE study_code = 'STD-DEL' AND sys_deleted = 0",
        Long.class);
    jdbc.update("""
        INSERT INTO hd_plt_study_milestone(
            study_id, stage_code, milestone_code, sys_create_by, sys_update_by)
        VALUES (?, 'PreIND', '1.1', 'seed', 'seed')
        """, studyId);

    mvc.perform(get("/api/v1/clinical-pipeline/studies/{id}/delete-preview", studyId)
            .with(authority("config.delete")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studyCode").value("STD-DEL"))
        .andExpect(jsonPath("$.milestoneCount").value(1))
        .andExpect(jsonPath("$.riskCount").value(0))
        .andExpect(jsonPath("$.teamCount").value(0))
        .andExpect(jsonPath("$.monthlyReportCount").value(0));

    mvc.perform(delete("/api/v1/clinical-pipeline/studies/{id}", studyId)
            .with(authority("config.delete")).with(csrf()))
        .andExpect(status().isNoContent());

    Integer milestoneCount = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_study_milestone WHERE study_id = ? AND sys_deleted = 0",
        Integer.class, studyId);
    Integer studyCount = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_study WHERE id = ? AND sys_deleted = 0",
        Integer.class, studyId);
    org.junit.jupiter.api.Assertions.assertEquals(0, milestoneCount);
    org.junit.jupiter.api.Assertions.assertEquals(0, studyCount);
  }

  @Test
  void rejectsDuplicateProjectCodeWithBusinessMessage() throws Exception {
    long programId = createProgram("PRG-DUP");
    createProject(programId, "PRJ-DUP");

    mvc.perform(post("/api/v1/clinical-pipeline/projects")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"PRJ-DUP","programId":%d,"indication":"重复项目",
                 "therapeuticAreaCode":"ONCOLOGY"}
                """.formatted(programId)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("PROJECT_CODE_EXISTS"))
        .andExpect(jsonPath("$.message").value("Project 编码已存在"));
  }

  private long createProgram(String code) throws Exception {
    String response = mvc.perform(post("/api/v1/clinical-pipeline/programs")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"%s","productName":"HD-001",
                 "moa":"PD-1","sourceCode":"SELF_DEVELOPED","originCode":"DOMESTIC"}
                """.formatted(code)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    JsonNode json = objectMapper.readTree(response);
    return json.path("id").asLong();
  }

  private long createProject(long programId, String code) throws Exception {
    String response = mvc.perform(post("/api/v1/clinical-pipeline/projects")
            .with(authority("config.create")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"code":"%s","programId":%d,
                 "indication":"非小细胞肺癌","therapeuticAreaCode":"ONCOLOGY"}
                """.formatted(code, programId)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response).path("id").asLong();
  }

  private static org.springframework.test.web.servlet.request.RequestPostProcessor authority(
      String permission) {
    return user("config-admin").authorities(new SimpleGrantedAuthority(permission));
  }
}
