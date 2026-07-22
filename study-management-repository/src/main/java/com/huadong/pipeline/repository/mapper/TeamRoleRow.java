package com.huadong.pipeline.repository.mapper;

public record TeamRoleRow(
    long id,
    String roleCode,
    String roleName,
    Long functionLineId,
    String functionCode,
    String functionName,
    int sortOrder) {
}
