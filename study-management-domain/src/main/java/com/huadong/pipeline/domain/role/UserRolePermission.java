package com.huadong.pipeline.domain.role;

public record UserRolePermission(long userId, long roleId, String permissionCode) {
}
