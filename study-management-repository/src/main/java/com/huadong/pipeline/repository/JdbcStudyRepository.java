package com.huadong.pipeline.repository;

import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.study.DuplicateStudyCodeException;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.StudyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcStudyRepository implements StudyRepository {
  private final JdbcClient jdbc;

  public JdbcStudyRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<Study> findAll() {
    return jdbc.sql("""
            SELECT id, code, name, indication, phase, status, owner_name, start_date, updated_at
            FROM biz_study ORDER BY updated_at DESC, id DESC LIMIT 500
            """)
        .query(StudyRow.class)
        .list()
        .stream()
        .map(row -> new Study(
            row.id(),
            row.code(),
            row.name(),
            row.indication(),
            row.phase(),
            StudyStatus.valueOf(row.status()),
            row.ownerName(),
            row.startDate(),
            row.updatedAt()))
        .toList();
  }

  @Override
  public long count() {
    return jdbc.sql("SELECT COUNT(*) FROM biz_study").query(Long.class).single();
  }

  @Override
  public long countByStatus(StudyStatus status) {
    return jdbc.sql("SELECT COUNT(*) FROM biz_study WHERE status = :status")
        .param("status", status.name())
        .query(Long.class)
        .single();
  }

  @Override
  public void save(Study study, String createdBy) {
    try {
      jdbc.sql("""
              INSERT INTO biz_study(code, name, indication, phase, status, owner_name, start_date, created_by)
              VALUES (:code, :name, :indication, :phase, :status, :ownerName, :startDate, :createdBy)
              """)
          .param("code", study.code())
          .param("name", study.name())
          .param("indication", study.indication())
          .param("phase", study.phase())
          .param("status", study.status().name())
          .param("ownerName", study.ownerName())
          .param("startDate", study.startDate())
          .param("createdBy", createdBy)
          .update();
    } catch (DuplicateKeyException ex) {
      throw new DuplicateStudyCodeException(ex);
    }
  }

  private record StudyRow(
      long id,
      String code,
      String name,
      String indication,
      String phase,
      String status,
      String ownerName,
      LocalDate startDate,
      LocalDateTime updatedAt) {
  }
}
