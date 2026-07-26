package com.huadong.pipeline.domain.study;

import com.huadong.pipeline.common.StudyStatus;
import java.util.List;

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

  void save(Study study, String createdBy);

  record StudyListQuery(
      String therapeuticArea,
      String program,
      String milestoneStatus,
      int page,
      int pageSize) {
    public StudyListQuery normalized() {
      int safePage = Math.max(1, page);
      int safeSize = Math.min(100, Math.max(1, pageSize));
      return new StudyListQuery(
          trim(therapeuticArea), trim(program), trim(milestoneStatus), safePage, safeSize);
    }

    public StudyListQuery withoutMilestoneStatus() {
      return new StudyListQuery(therapeuticArea, program, "", page, pageSize);
    }

    public StudyListQuery withPaging(int page, int pageSize) {
      return new StudyListQuery(therapeuticArea, program, milestoneStatus, page, pageSize);
    }

    private static String trim(String value) {
      return value == null ? "" : value.trim();
    }
  }

  record StudyPage(List<Study> data, long totalItems, int page, int pageSize) {}
}
