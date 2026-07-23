package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.setting.SettingRepository;
import com.huadong.pipeline.domain.study.DuplicateStudyCodeException;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.PipelineOverviewRepository;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.study.StudyRepository;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.LocalDate;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RepositoryContractIntegrationTest {
  @Autowired SettingRepository settings;
  @Autowired StudyRepository studies;
  @Autowired UserAccountRepository users;
  @Autowired PipelineOverviewRepository overviewRepository;
  @Autowired JdbcTemplate jdbc;

  @Test
  void settingsStaySortedFilteredAndLimitedWithoutUpsertingMissingKeys() {
    jdbc.update("DELETE FROM hd_plt_system_setting");
    IntStream.range(0, 101).forEach(index -> jdbc.update("""
        INSERT INTO hd_plt_system_setting(
            config_key, config_value, config_description, public_visible,
            sys_create_by, sys_update_by)
        VALUES (?, ?, ?, ?, ?, ?)
        """, "setting-%03d".formatted(100 - index), "value", "description",
        index % 2 == 0, "seed", "seed"));

    var all = settings.findAll();
    var publicSettings = settings.findPublic();

    assertThat(all).hasSize(100);
    assertThat(all).extracting(setting -> setting.configKey()).isSorted();
    assertThat(publicSettings).hasSize(51);
    assertThat(publicSettings).allMatch(setting -> setting.publicVisible());

    settings.update("setting-000", "changed", "admin@example.com");
    assertThat(settings.findByKey("setting-000")).get()
        .satisfies(setting -> {
          assertThat(setting.configValue()).isEqualTo("changed");
          assertThat(setting.updatedBy()).isEqualTo("admin@example.com");
          assertThat(setting.updatedAt()).isNotNull();
        });

    settings.update("missing", "ignored", "admin@example.com");
    assertThat(settings.findByKey("missing")).isEmpty();
  }

  @Test
  void usersStaySortedAndLimitedAndCanBeFoundByUsername() {
    jdbc.update("DELETE FROM hd_plt_user_role");
    jdbc.update("DELETE FROM hd_plt_user");
    IntStream.range(0, 501).forEach(index -> users.create(
        "user-%03d@example.com".formatted(index),
        "hash-%03d".formatted(index),
        "User %03d".formatted(index),
        "USER"));

    var result = users.findAll();

    assertThat(result).hasSize(500);
    assertThat(result).extracting(user -> user.id()).isSorted();
    assertThat(result.getFirst().username()).isEqualTo("user-000@example.com");
    assertThat(result.getLast().username()).isEqualTo("user-499@example.com");
    assertThat(users.findByUsername("USER-250@EXAMPLE.COM")).get()
        .satisfies(user -> assertThat(user.passwordHash()).isEqualTo("hash-250"));
    assertThat(users.findByUsername("missing@example.com")).isEmpty();
    assertThatThrownBy(() -> users.create(
        "user-250@example.com", "other-hash", "Duplicate", "USER"))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void studiesUseHierarchySnapshotsDerivedStatusAndDuplicateCodeTranslation() {
    seedStudyHierarchy();
    studies.save(study("STUDY-OLDER", true), "seed@example.com");
    studies.save(study("STUDY-NEWER", false), "seed@example.com");
    jdbc.update("UPDATE hd_plt_study SET sys_update_time = ? WHERE study_code = ?",
        LocalDate.of(2026, 1, 1).atStartOfDay(), "STUDY-OLDER");
    jdbc.update("UPDATE hd_plt_study SET sys_update_time = ? WHERE study_code = ?",
        LocalDate.of(2026, 2, 1).atStartOfDay(), "STUDY-NEWER");

    assertThat(studies.findAll()).extracting(Study::code)
        .containsExactly("STUDY-NEWER", "STUDY-OLDER");
    assertThat(studies.count()).isEqualTo(2);
    assertThat(studies.countByStatus(StudyStatus.ACTIVE)).isEqualTo(1);
    assertThat(studies.countByStatus(StudyStatus.PLANNED)).isEqualTo(1);
    jdbc.update("""
        INSERT INTO hd_plt_team_assignment(study_id, user_id)
        SELECT id, 42 FROM hd_plt_study WHERE study_code = 'STUDY-OLDER'
        """);
    var assignedScope = StudyAccessScope.assignedTo(42);
    assertThat(studies.findAll(assignedScope)).extracting(Study::code)
        .containsExactly("STUDY-OLDER");
    assertThat(studies.count(assignedScope)).isEqualTo(1);
    assertThat(studies.countByStatus(StudyStatus.ACTIVE, assignedScope)).isEqualTo(1);
    assertThat(studies.countByStatus(StudyStatus.PLANNED, assignedScope)).isZero();
    assertThat(studies.findAll()).filteredOn(study -> study.code().equals("STUDY-OLDER"))
        .singleElement().satisfies(study -> {
          assertThat(study.indication()).isEqualTo("实体瘤");
          assertThat(study.programCode()).isEqualTo("PROGRAM-001");
          assertThat(study.projectCode()).isEqualTo("PROJECT-001");
          assertThat(study.therapeuticAreaCode()).isEqualTo("ONCOLOGY");
          assertThat(study.ownerName()).isEqualTo("seed@example.com");
        });

    var generated = study("STUDY-GENERATED", true);
    studies.save(generated, "tester@example.com");
    assertThat(studies.findAll())
        .filteredOn(study -> study.code().equals("STUDY-GENERATED"))
        .singleElement()
        .satisfies(study -> {
          assertThat(study.id()).isPositive();
          assertThat(study.updatedAt()).isNotNull();
        });

    assertThatThrownBy(() -> studies.save(
        study("STUDY-OLDER", true), "tester@example.com"))
        .isInstanceOf(DuplicateStudyCodeException.class);
  }

  @Test
  void studyListKeepsItsFiveHundredRowLimit() {
    seedStudyHierarchy();
    IntStream.range(0, 501).forEach(index -> studies.save(
        study("STUDY-%03d".formatted(index), true),
        "seed@example.com"));

    assertThat(studies.findAll()).hasSize(500);
  }

  @Test
  void pipelineOverviewGroupsProjectsByAreaWithStudiesAndHonoursDataScope() {
    seedStudyHierarchy();
    studies.save(study("STUDY-ACTIVE", true), "seed@example.com");
    studies.save(study("STUDY-PLANNED", false), "seed@example.com");

    var all = overviewRepository.findOverviewProjects(StudyAccessScope.all());
    assertThat(all).hasSize(1);
    assertThat(all.getFirst().code()).isEqualTo("PROJECT-001");
    assertThat(all.getFirst().therapeuticAreaCode()).isEqualTo("ONCOLOGY");
    assertThat(all.getFirst().therapeuticAreaName()).isEqualTo("肿瘤");
    assertThat(all.getFirst().studies()).hasSize(2);

    jdbc.update("""
        INSERT INTO hd_plt_team_assignment(study_id, user_id)
        SELECT id, 42 FROM hd_plt_study WHERE study_code = 'STUDY-ACTIVE'
        """);
    var assigned = overviewRepository.findOverviewProjects(StudyAccessScope.assignedTo(42));
    assertThat(assigned).hasSize(1);
    assertThat(assigned.getFirst().studies())
        .extracting(study -> study.code())
        .containsExactly("STUDY-ACTIVE");
  }

  private Study study(String code, boolean active) {
    var start = LocalDate.of(2026, 1, 1);
    return Study.create(
        code, "PROGRAM-001", "PROJECT-001", "ONCOLOGY", "PHASE_1",
        start, LocalDate.of(2026, 12, 31), active ? start : null, null, "Description");
  }

  private void seedStudyHierarchy() {
    jdbc.update("DELETE FROM hd_plt_team_assignment");
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
