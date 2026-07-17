package com.huadong.pipeline.api;

import com.huadong.pipeline.common.StudyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StudyApi {
  PipelineOverviewResponse overview();

  List<StudyResponse> list();

  void create(@Valid CreateStudyRequest request, String username);

  record CreateStudyRequest(
      @NotBlank @Size(max = 50) String code,
      @NotBlank @Size(max = 200) String name,
      @NotBlank @Size(max = 200) String indication,
      @NotBlank @Size(max = 30) String phase,
      @NotNull StudyStatus status,
      @NotBlank @Size(max = 100) String ownerName,
      LocalDate startDate) {
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
