package com.huadong.pipeline.domain.audit;

public record AuditContext(
    Long operatorUserId,
    String operatorEmail,
    String operatorDisplayName,
    String requestId,
    String ipAddress,
    String requestMethod,
    String requestPath) {
}
