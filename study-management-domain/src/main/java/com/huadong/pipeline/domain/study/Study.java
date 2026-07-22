package com.huadong.pipeline.domain.study;

import com.huadong.pipeline.common.StudyStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Study(
    long id,
    String code,
    String indication,
    String phase,
    StudyStatus status,
    String ownerName,
    LocalDate startDate,
    LocalDateTime updatedAt,
    String programCode,
    String projectCode,
    String therapeuticAreaCode,
    LocalDate plannedEndDate,
    LocalDate actualStartDate,
    LocalDate actualEndDate,
    String description) {

  public static Study create(
      String code,
      String programCode,
      String projectCode,
      String therapeuticAreaCode,
      String phase,
      LocalDate plannedStartDate,
      LocalDate plannedEndDate,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      String description) {
    StudyStatus status = actualEndDate != null
        ? StudyStatus.COMPLETED
        : actualStartDate != null ? StudyStatus.ACTIVE : StudyStatus.PLANNED;
    return new Study(
        0,
        code.trim(),
        "",
        phase.trim(),
        status,
        "",
        plannedStartDate,
        null,
        programCode.trim(),
        projectCode.trim(),
        therapeuticAreaCode.trim(),
        plannedEndDate,
        actualStartDate,
        actualEndDate,
        description == null ? null : description.trim());
  }
}
