package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface TeamMatrixApi {
  MatrixResponse list(
      String username, String studyQuery, String roleQuery, int page, int pageSize);

  BatchResponse replace(@Valid BatchRequest request, String username);

  record StudyResponse(
      long studyId,
      String studyCode,
      String indication,
      String statusCode,
      String statusLabel,
      long version) {
  }

  record RoleResponse(
      String roleCode,
      String roleName,
      String functionCode,
      String functionName) {
  }

  record MemberResponse(
      long userId,
      String email,
      String displayName,
      boolean enabled) {
  }

  record AssignmentResponse(
      long studyId,
      String roleCode,
      List<MemberResponse> members) {
  }

  record PaginationResponse(
      int page,
      int pageSize,
      long totalItems,
      int totalPages) {
  }

  record MatrixResponse(
      List<StudyResponse> studies,
      List<RoleResponse> roles,
      List<AssignmentResponse> assignments,
      int totalRoles,
      PaginationResponse pagination) {
  }

  record RoleChangeRequest(
      @NotBlank @Size(max = 64) String roleCode,
      @NotNull @Size(max = 100) List<@Min(1) Long> userIds) {
  }

  record StudyChangeRequest(
      @Min(1) long studyId,
      @Min(0) long expectedVersion,
      @NotEmpty @Size(max = 44) List<@Valid RoleChangeRequest> roles) {
  }

  record BatchRequest(
      @NotEmpty @Size(max = 20) List<@Valid StudyChangeRequest> studies) {
  }

  record StudyVersionResponse(long studyId, long version) {
  }

  record BatchResponse(List<StudyVersionResponse> studies) {
  }
}
