package com.huadong.pipeline.audit;

public record AuditRequestMetadata(
    String requestId,
    String ipAddress,
    String requestMethod,
    String requestPath) {
}
