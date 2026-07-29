package com.huadong.pipeline.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AuditLogApi {
  AuditLogPageResponse list(
      AuditLogQuery query, String username, Set<String> authorities);

  record AuditLogQuery(
      String moduleCode,
      String subjectType,
      Long subjectId,
      Long scopeStudyId,
      String groupType,
      Long groupId,
      String groupCode,
      String resultCode,
      int page,
      int pageSize) {
  }

  record AuditLogPageResponse(
      List<AuditLogResponse> data,
      int page,
      int pageSize,
      long totalItems,
      long totalPages) {
  }

  record AuditLogResponse(
      long id,
      String moduleCode,
      String subjectType,
      Long subjectId,
      String subjectCode,
      String actionCode,
      String actionLabel,
      String resultCode,
      String operationReason,
      String errorCode,
      Long operatorUserId,
      String operatorEmail,
      String operatorDisplayName,
      String requestId,
      String ipAddress,
      String requestMethod,
      String requestPath,
      String targetTable,
      Long targetId,
      int payloadVersion,
      boolean historicalSnapshotMissing,
      Map<String, Object> beforeData,
      Map<String, Object> afterData,
      List<FieldChangeResponse> changes,
      LocalDateTime occurredTime) {
  }

  record FieldChangeResponse(
      String fieldName,
      String fieldLabel,
      Object beforeValue,
      Object afterValue) {
  }
}
