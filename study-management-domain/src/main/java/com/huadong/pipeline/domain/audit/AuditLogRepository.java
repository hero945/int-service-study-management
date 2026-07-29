package com.huadong.pipeline.domain.audit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditLogRepository {
  AuditPage findPage(AuditQuery query);

  void insert(AuditEvent event);

  record AuditQuery(
      String moduleCode,
      String subjectType,
      Long subjectId,
      Long scopeStudyId,
      String groupType,
      Long groupId,
      String groupCode,
      String resultCode,
      int page,
      int pageSize,
      boolean restrictStudyScope,
      long userId) {
  }

  record AuditPage(List<AuditLogRecord> data, int page, int pageSize, long totalItems) {
    public AuditPage {
      data = List.copyOf(data);
    }
  }

  record AuditLogRecord(
      long id,
      String moduleCode,
      String subjectType,
      Long subjectId,
      String subjectCode,
      Long scopeStudyId,
      String actionCode,
      String targetTable,
      Long targetId,
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
      int payloadVersion,
      Map<String, Object> beforeData,
      Map<String, Object> afterData,
      LocalDateTime occurredTime) {
  }
}
