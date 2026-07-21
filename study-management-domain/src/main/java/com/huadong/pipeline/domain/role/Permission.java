package com.huadong.pipeline.domain.role;

public record Permission(
    long id,
    String moduleCode,
    String code,
    String name,
    String type,
    String actionCode,
    String description,
    int sortOrder) {
}
