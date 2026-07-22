package com.huadong.pipeline.repository.mapper;

public record TeamStudyRow(
    long studyId,
    String studyCode,
    String indication,
    String statusCode,
    String statusLabel,
    long teamVersion) {
}
