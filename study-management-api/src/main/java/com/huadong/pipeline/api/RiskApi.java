package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface RiskApi {
  PageResponse list(String username, String query, String functionCode, String status,
                    String level, Long studyId, Long ownerUserId, Boolean overdueOnly,
                    String sortBy, String sortOrder, int page, int pageSize);
  DetailResponse detail(String username, String riskCode);
  FormOptionsResponse formOptions(String username, Long studyId);
  DetailResponse create(@Valid CreateRequest request, String username);
  DetailResponse update(String riskCode, @Valid UpdateRequest request, String username);
  void delete(String riskCode, long expectedVersion, String username);
  DetailResponse addAction(String riskCode, @Valid ActionCreateRequest request, String username);
  DetailResponse updateAction(String riskCode, long actionId,
                              @Valid ActionUpdateRequest request, String username);
  DetailResponse deleteAction(String riskCode, long actionId, long expectedVersion,
                              String username);

  record SummaryResponse(String riskCode, long studyId, String studyCode, String programCode,
      String projectCode, String functionCode, String functionName, String description,
      long ownerUserId, String ownerName, int score, String level, String status,
      int actionCount, int openActionCount, int overdueActionCount, LocalDate nextPlannedDate,
      long version, Instant updatedAt) {}
  record StatsResponse(long total, long open, long high, long medium) {}
  record PaginationResponse(int page, int pageSize, long totalItems, int totalPages) {}
  record PageResponse(List<SummaryResponse> data, StatsResponse stats,
                      PaginationResponse pagination) {}
  record AssessmentResponse(long id, int number, int impact, int likelihood,
      int detectability, int score, String level, String reason,
      String assessedBy, Instant assessedAt) {}
  record ActionResponse(long id, String description, long ownerUserId, String ownerName,
      LocalDate plannedDate, LocalDate completedDate, String status,
      String completionNote, long version, boolean overdue) {}
  record ActivityResponse(String type, String title, String detail, Instant at, String by) {}
  record DetailResponse(SummaryResponse risk, LocalDate registeredDate, String closeReason,
      Instant closedTime, List<AssessmentResponse> assessments, List<ActionResponse> actions,
      List<ActivityResponse> activities) {}
  record StudyOptionResponse(long id, String studyCode, String programCode, String projectCode) {}
  record FunctionOptionResponse(long id, String code, String name) {}
  record MemberOptionResponse(long id, String email, String displayName) {}
  /** Active scoring thresholds from hd_plt_risk_rule_version. */
  record ScoringRuleResponse(long id, int lowMax, int mediumMax) {}
  record FormOptionsResponse(List<StudyOptionResponse> studies,
      List<FunctionOptionResponse> functions, List<MemberOptionResponse> owners,
      ScoringRuleResponse scoringRule) {}

  record AssessmentRequest(
      @Min(1) @Max(5) int impact,
      @Min(1) @Max(5) int likelihood,
      @Min(1) @Max(5) int detectability,
      @Size(max = 1000) String reason) {}
  record ActionInput(
      @NotBlank @Size(max = 4000) String description,
      @Min(1) long ownerUserId,
      LocalDate plannedDate,
      LocalDate completedDate,
      @Size(max = 32) String status,
      @Size(max = 2000) String completionNote,
      @Size(max = 2000) String changeReason) {}
  record CreateRequest(
      @Min(1) long studyId,
      @Min(1) long functionLineId,
      @Min(1) long ownerUserId,
      @NotBlank @Size(max = 4000) String description,
      LocalDate registeredDate,
      @NotNull @Valid AssessmentRequest assessment,
      @Size(max = 50) List<@Valid ActionInput> actions) {}
  record UpdateRequest(
      @Min(0) long expectedVersion,
      @Min(1) long studyId,
      @Min(1) long functionLineId,
      @Min(1) long ownerUserId,
      @NotBlank @Size(max = 4000) String description,
      LocalDate registeredDate,
      @NotBlank @Size(max = 20) String status,
      @Size(max = 2000) String statusReason,
      @Valid AssessmentRequest assessment) {}
  record ActionCreateRequest(
      @Min(0) long expectedRiskVersion,
      @NotNull @Valid ActionInput action) {}
  record ActionUpdateRequest(
      @Min(0) long expectedVersion,
      @NotNull @Valid ActionInput action) {}
}
