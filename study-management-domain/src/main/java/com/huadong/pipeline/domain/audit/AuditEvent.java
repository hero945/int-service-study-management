package com.huadong.pipeline.domain.audit;

import java.util.Map;

public record AuditEvent(
    String moduleCode,
    String subjectType,
    Long subjectId,
    String subjectCode,
    Long scopeStudyId,
    String groupType,
    Long groupId,
    String groupCode,
    String actionCode,
    String targetTable,
    Long targetId,
    String resultCode,
    String operationReason,
    String errorCode,
    int payloadVersion,
    Map<String, Object> beforeData,
    Map<String, Object> afterData,
    AuditContext context) {
}
