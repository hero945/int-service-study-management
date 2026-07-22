package com.huadong.pipeline.repository.mapper;

public record TeamMemberRow(
    long studyId,
    String roleCode,
    long userId,
    String email,
    String displayName,
    String statusCode) {
}
