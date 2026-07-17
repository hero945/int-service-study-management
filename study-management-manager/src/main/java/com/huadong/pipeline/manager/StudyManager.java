package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.study.DuplicateStudyCodeException;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.StudyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyManager {
  private final StudyRepository studies;

  public StudyManager(StudyRepository studies) {
    this.studies = studies;
  }

  public List<StudyView> list() {
    return studies.findAll().stream()
        .map(study -> new StudyView(
            study.id(),
            study.code(),
            study.name(),
            study.indication(),
            study.phase(),
            study.status(),
            study.ownerName(),
            study.startDate(),
            study.updatedAt()))
        .toList();
  }

  public PipelineOverview overview() {
    List<StatusMetric> metrics = Arrays.stream(StudyStatus.values())
        .map(status -> new StatusMetric(
            status,
            studies.countByStatus(status)))
        .toList();
    return new PipelineOverview("临床研发管线", studies.count(), metrics);
  }

  @Transactional
  public void create(CreateStudyCommand command, String username) {
    Study study = Study.create(
        command.code(),
        command.name(),
        command.indication(),
        command.phase(),
        command.status(),
        command.ownerName(),
        command.startDate());
    try {
      studies.save(study, username);
    } catch (DuplicateStudyCodeException ex) {
      throw new BusinessException("STUDY_CODE_EXISTS", "项目编号已存在");
    }
  }

  public record CreateStudyCommand(
      String code,
      String name,
      String indication,
      String phase,
      StudyStatus status,
      String ownerName,
      LocalDate startDate) {
  }

  public record StatusMetric(StudyStatus status, long count) {
  }

  public record PipelineOverview(String title, long total, List<StatusMetric> statuses) {
  }

  public record StudyView(
      long id,
      String code,
      String name,
      String indication,
      String phase,
      StudyStatus status,
      String ownerName,
      LocalDate startDate,
      LocalDateTime updatedAt) {
  }
}
