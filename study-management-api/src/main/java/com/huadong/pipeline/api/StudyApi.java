package com.huadong.pipeline.api;

import com.huadong.pipeline.common.StudyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StudyApi {
  PipelineOverviewResponse overview(String username);

  List<StudyResponse> list(String username);

  void create(@Valid CreateStudyRequest request, String username);

  record CreateStudyRequest(
      @NotBlank @Size(max = 64) String code,
      @NotBlank @Size(max = 200) String name,
      @NotBlank @Size(max = 64) String programCode,
      @NotBlank @Size(max = 64) String projectCode,
      @NotBlank @Size(max = 64) String therapeuticAreaCode,
      @NotBlank @Size(max = 32) String phase,
      LocalDate plannedStartDate,
      LocalDate plannedEndDate,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      @Size(max = 5000) String description) {
  }

  record StudyResponse(
      long id,
      String code,
      String name,
      String indication,
      String phase,
      String status,
      String statusLabel,
      String statusTone,
      String ownerName,
      LocalDate startDate,
      LocalDateTime updatedAt) {
  }

  record StatusMetricResponse(String status, String label, String tone, long count) {
  }

  record PipelineOverviewResponse(
      String title,
      long total,
      List<StatusMetricResponse> statuses) {
  }
}
