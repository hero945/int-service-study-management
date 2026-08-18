package com.huadong.pipeline.domain.study;

import com.huadong.pipeline.common.StudyStatus;
import java.util.List;
import java.util.Optional;

public interface StudyRepository {
  List<Study> findAll(StudyAccessScope accessScope);

  /** Paged study list with optional TA / Program filters (milestone status filtered upstream). */
  StudyPage findPage(StudyAccessScope accessScope, StudyListQuery query);

  long count(StudyAccessScope accessScope);

  long countByStatus(StudyStatus status, StudyAccessScope accessScope);

  default List<Study> findAll() {
    return findAll(StudyAccessScope.all());
  }

  default long count() {
    return count(StudyAccessScope.all());
  }

  default long countByStatus(StudyStatus status) {
    return countByStatus(status, StudyAccessScope.all());
  }

  Optional<Integer> findMaxVersionByCode(String code);

  Optional<Study> findByCode(String code);

  void save(Study study, int version, String createdBy);

  record StudyListQuery(
      String therapeuticArea,
      String program,
      String product,
      String project,
      String studyCode,
      String milestoneStatus,
      int page,
      int pageSize) {
    public StudyListQuery normalized() {
      int safePage = Math.max(1, page);
      int safeSize = Math.min(100, Math.max(1, pageSize));
      return new StudyListQuery(
          trim(therapeuticArea),
          trim(program),
          trim(product),
          trim(project),
          trim(studyCode),
          trim(milestoneStatus),
          safePage,
          safeSize);
    }

    public StudyListQuery withoutMilestoneStatus() {
      return new StudyListQuery(
          therapeuticArea, program, product, project, studyCode, "", page, pageSize);
    }

    public StudyListQuery withPaging(int page, int pageSize) {
      return new StudyListQuery(
          therapeuticArea, program, product, project, studyCode, milestoneStatus, page, pageSize);
    }

    private static String trim(String value) {
      return value == null ? "" : value.trim();
    }
  }

  record StudyPage(List<Study> data, long totalItems, int page, int pageSize) {}
}
