package com.huadong.pipeline.domain.study;

import com.huadong.pipeline.common.StudyStatus;
import java.util.List;

public interface StudyRepository {
  List<Study> findAll();

  long count();

  long countByStatus(StudyStatus status);

  void save(Study study, String createdBy);
}
