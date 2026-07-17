package com.huadong.pipeline.domain.study;

import com.huadong.pipeline.common.StudyStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Study(
    long id,
    String code,
    String name,
    String indication,
    String phase,
    StudyStatus status,
    String ownerName,
    LocalDate startDate,
    LocalDateTime updatedAt) {

  public static Study create(
      String code,
      String name,
      String indication,
      String phase,
      StudyStatus status,
      String ownerName,
      LocalDate startDate) {
    return new Study(
        0,
        code.trim(),
        name.trim(),
        indication.trim(),
        phase.trim(),
        status,
        ownerName.trim(),
        startDate,
        null);
  }
}
