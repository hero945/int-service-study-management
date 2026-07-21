package com.huadong.pipeline.domain.study;

import com.huadong.pipeline.common.StudyStatus;
import java.util.List;

public interface StudyRepository {
  List<Study> findAll(StudyAccessScope accessScope);

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
}
